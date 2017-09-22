package amf.graph

import amf.document.{BaseUnit, Document, Module}
import amf.domain._
import amf.domain.extensions._
import amf.metadata.Type.{Array, Bool, Iri, RegExp, SortedArray, Str}
import amf.metadata.document.BaseUnitModel.Location
import amf.metadata.document.{BaseUnitModel, DocumentModel, ModuleModel}
import amf.metadata.domain._
import amf.metadata.shape._
import amf.metadata.{Field, Obj, Type}
import amf.model.{AmfElement, AmfObject, AmfScalar}
import amf.parser.{YMapOps, YValueOps}
import amf.shape._
import org.yaml.model._

/**
  * AMF Graph parser
  */
object GraphParser extends GraphParserHelpers {

  def parse(document: YDocument, location: String): BaseUnit = {
    val parser = Parser(Map())
    parser.parse(document, location)
  }

  case class Parser(var nodes: Map[String, AmfElement]) {

    val dynamicGraphParser = new DynamicGraphParser(nodes)

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

      val instance = builders(model)(annotations(nodes, sources, id))
      instance.withId(id)

      model.fields.foreach(f => {
        val k = f.value.iri()
        map.key(k) match {
          case Some(entry) => traverse(instance, f, value(f.`type`, entry.value.value), sources, k)
          case _           =>
        }
      })

      // parsing custom extensions
      instance match {
        case elm: DomainElement => parseCustomProperties(map, elm)
        case _                  => // ignore
      }

      nodes = nodes + (id -> instance)
      instance
    }

    private def parseCustomProperties(map: YMap, instance: DomainElement) = {
      val customProperties: Seq[String] = map.key(DomainElementModel.CustomDomainProperties.value.iri()) match {
        case Some(entry) =>
          entry.value.value match {
            case sequence: YSequence =>
              sequence.values.flatMap(_.toMap.key("@id")).map(_.value.value.toScalar.text)
            case _ => Seq()
          }
        case _ => Seq()
      }

      val domainExtensions: Seq[DomainExtension] = customProperties
        .flatMap { propertyUri =>
          map
            .key(propertyUri)
            .map(entry => {
              val parsedNode      = dynamicGraphParser.parseDynamicType(entry.value.value.toMap)
              val domainExtension = DomainExtension()
              val domainProperty  = CustomDomainProperty()
              domainProperty.id = propertyUri
              domainExtension.withId(parsedNode.id)
              domainExtension.withDefinedBy(domainProperty)
              domainExtension.withExtension(parsedNode)
              domainExtension
            })
        }

      if (domainExtensions.nonEmpty) {
        instance.withCustomDomainProperties(domainExtensions)
      }
    }

    private def traverse(instance: AmfObject, f: Field, node: YValue, sources: SourceMap, key: String) = {
      f.`type` match {
        case _: Obj             => instance.set(f, parse(node.toMap), annotations(nodes, sources, key))
        case Str | RegExp | Iri => instance.set(f, str(node.toScalar), annotations(nodes, sources, key))
        case Bool               => instance.set(f, bool(node.toScalar), annotations(nodes, sources, key))
        case Type.Int           => instance.set(f, int(node.toScalar), annotations(nodes, sources, key))
        case l: SortedArray     => instance.setArray(f, parseList(l.element, node.toMap), annotations(nodes, sources, key))
        case a: Array =>
          val items = node.toSequence.values
          val values: Seq[AmfElement] = a.element match {
            case _: Obj    => items.map(n => parse(n.toMap))
            case Str | Iri => items.map(n => str(value(a.element, n).toScalar))
          }
          a.element match {
            case _: BaseUnitModel => instance.setArrayWithoutId(f, values, annotations(nodes, sources, key))
            case _                => instance.setArray(f, values, annotations(nodes, sources, key))
          }
      }
    }
  }

  private def str(node: YScalar) = AmfScalar(node.text)

  private def bool(node: YScalar) = AmfScalar(node.text.toBoolean)

  private def int(node: YScalar) = AmfScalar(node.text.toInt)

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
    PropertyDependenciesModel -> PropertyDependencies.apply,
    ModuleModel               -> Module.apply
  )

  private val types: Map[String, Obj] = builders.keys.map(t => t.`type`.head.iri() -> t).toMap
}
