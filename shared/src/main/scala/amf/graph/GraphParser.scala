package amf.graph

import amf.common.core.Strings
import amf.document.{BaseUnit, Document}
import amf.domain._
import amf.metadata.SourceMapModel.{Element, Value}
import amf.metadata.Type.{Array, Bool, Iri, RegExp, SortedArray, Str}
import amf.metadata.document.BaseUnitModel.Location
import amf.metadata.document.DocumentModel
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.domain._
import amf.metadata.shape._
import amf.metadata.{Field, Obj, SourceMapModel, Type}
import amf.model.{AmfElement, AmfObject, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape._
import amf.vocabulary.Namespace.SourceMaps
import org.yaml.model._

import scala.collection.mutable

/**
  * AMF Graph parser
  */
object GraphParser {

  def parse(document: YDocument, location: String): BaseUnit = {
    val parser = Parser(Map())
    parser.parse(document, location)
  }

  case class Parser(var nodes: Map[String, AmfElement]) {

    def parse(document: YDocument, location: String): BaseUnit = {
      document.value.flatMap(_.toSequence.values.headOption).map(_.toMap) match {
        case Some(root) => parse(root).set(Location, location).asInstanceOf[BaseUnit]
        case _          => throw new Exception(s"Unable to parse $document")
      }
    }

    private def retrieveType(map: YMap): Obj =
      ts(map).find(types.get(_).isDefined) match {
        case Some(t) => types(t)
        case None    => throw new Exception(s"Error parsing JSON-LD node, unknown @types ${ts(map)}")
      }

    private def parseList(listElement: Type, node: YMap) = {
      retrieveElements(node).map({ (n) =>
        listElement match {
          case _: Obj => parse(n.toMap)
          case _      => str(value(listElement, n).toScalar)
        }
      })
    }

    private def retrieveElements(map: YMap): Seq[YValue] = {
      map.key("@list") match {
        case Some(entry) => entry.value.value.toSequence.values
        case _           => throw new Exception(s"No @list declaration on list node $map")
      }
    }

    private def parse(map: YMap): AmfObject = {
      val id      = retrieveId(map)
      val sources = retrieveSources(id, map)
      val model   = retrieveType(map)

      val instance = builders(model)(annotations(sources, id))
      instance.withId(id)

      model.fields.foreach(f => {
        val k = f.value.iri()
        map.key(k) match {
          case Some(entry) => traverse(instance, f, value(f.`type`, entry.value.value), sources, k)
          case _           =>
        }
      })

      nodes = nodes + (id -> instance)
      instance
    }

    private def value(t: Type, node: YValue): YValue = {
      node match {
        case s: YSequence =>
          t match {
            case Array(_) => node
            case _        => value(t, s.values.head)
          }
        case m: YMap =>
          t match {
            case Iri                            => m.key("@id").get.value.value
            case Str | RegExp | Bool | Type.Int => m.key("@value").get.value.value
            case _                              => node
          }
        case _ => node
      }
    }

    private def traverse(instance: AmfObject, f: Field, node: YValue, sources: SourceMap, key: String) = {
      f.`type` match {
        case _: Obj             => instance.set(f, parse(node.toMap), annotations(sources, key))
        case Str | RegExp | Iri => instance.set(f, str(node.toScalar), annotations(sources, key))
        case Bool               => instance.set(f, bool(node.toScalar), annotations(sources, key))
        case Type.Int           => instance.set(f, int(node.toScalar), annotations(sources, key))
        case l: SortedArray     => instance.setArray(f, parseList(l.element, node.toMap), annotations(sources, key))
        case a: Array =>
          val items = node.toSequence.values
          val values: Seq[AmfElement] = a.element match {
            case _: Obj    => items.map(n => parse(n.toMap))
            case Str | Iri => items.map(n => str(value(a.element, n).toScalar))
          }
          instance.setArray(f, values, annotations(sources, key))
      }
    }

    private def retrieveSources(id: String, map: YMap): SourceMap = {
      map.key(Sources.value.iri()) match {
        case Some(entry) => parseSourceNode(value(SourceMapModel, entry.value.value).toMap)
        case _           => SourceMap.empty
      }
    }

    private def parseSourceNode(map: YMap): SourceMap = {
      val result = SourceMap()
      map.entries.foreach(entry => {
        entry.key.value.toScalar.text.unquote match {
          case AnnotationName(annotation) =>
            val consumer = result.annotation(annotation)
            entry.value.value.toSequence.values.foreach(entry => {
              val element = entry.toMap
              val k       = element.key(Value.value.iri()).get
              val v       = element.key(Element.value.iri()).get
              consumer(value(Value.`type`, k.key.value).toScalar.text.unquote,
                       value(Element.`type`, v.value.value).toScalar.text.unquote)
            })
          case _ => // Unknown annotation identifier
        }
      })
      result
    }

    private def annotations(sources: SourceMap, key: String): Annotations = {
      val result = Annotations()

      if (sources.nonEmpty) {
        sources.annotations.foreach {
          case (annotation, values: mutable.Map[String, String]) =>
            annotation match {
              case Annotation(deserialize) if values.contains(key) => result += deserialize(values(key), nodes)
              case _                                               =>
            }
        }
      }

      result
    }

    private def retrieveId(ast: YMap): String = {
      ast.key("@id") match {
        case Some(entry) => entry.value.value.toScalar.text.unquote
        case _           => throw new Exception(s"No @id declaration on node $ast")
      }
    }

    private def ts(map: YMap): Seq[String] = {
      map.key("@type") match {
        case Some(entry) => entry.value.value.toSequence.values.map(_.toScalar.text.unquote)
        case _           => throw new Exception(s"No @type declaration on node $map")
      }
    }

    private object AnnotationName {
      def unapply(uri: String): Option[String] = uri match {
        case url if url.startsWith(SourceMaps.base) => Some(url.substring(url.indexOf("#") + 1))
        case _                                      => None
      }
    }
  }

  private def str(node: YScalar) = AmfScalar(node.text.unquote)

  private def bool(node: YScalar) = AmfScalar(node.text.unquote.toBoolean)

  private def int(node: YScalar) = AmfScalar(node.text.unquote.toInt)

  /** Object Type builders. */
  private val builders: Map[Obj, (Annotations) => AmfObject] = Map(
    DocumentModel             -> Document.apply,
    WebApiModel               -> WebApi.apply,
    OrganizationModel         -> Organization.apply,
    LicenseModel              -> License.apply,
    CreativeWorkModel         -> CreativeWork.apply,
    EndPointModel             -> EndPoint.apply,
    OperationModel            -> Operation.apply,
    ParameterModel            -> Parameter.apply,
    PayloadModel              -> Payload.apply,
    RequestModel              -> Request.apply,
    ResponseModel             -> Response.apply,
    NodeShapeModel            -> NodeShape.apply,
    ArrayShapeModel           -> ArrayShape.apply,
    ScalarShapeModel          -> ScalarShape.apply,
    PropertyShapeModel        -> PropertyShape.apply,
    XMLSerializerModel        -> XMLSerializer.apply,
    PropertyDependenciesModel -> PropertyDependencies.apply
  )

  private val types: Map[String, Obj] = builders.keys.map(t => t.`type`.head.iri() -> t).toMap
}
