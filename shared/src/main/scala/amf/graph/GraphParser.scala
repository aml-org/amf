package amf.graph

import amf.common.AMFAST
import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.common.core.Strings
import amf.document.{BaseUnit, Document}
import amf.domain._
import amf.metadata.SourceMapModel.{Element, Value}
import amf.metadata.Type.{Array, Bool, Iri, RegExp, Str}
import amf.metadata.document.BaseUnitModel.Location
import amf.metadata.document.DocumentModel
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.domain._
import amf.metadata.shape._
import amf.metadata.{Field, Obj, SourceMapModel, Type}
import amf.model.{AmfElement, AmfObject, AmfScalar}
import amf.shape._
import amf.vocabulary.Namespace
import amf.vocabulary.Namespace.SourceMaps

import scala.collection.mutable

/**
  * AMF Graph parser
  */
object GraphParser {

  def parse(ast: AMFAST, location: String): BaseUnit = {
    val parser = Parser(Map())
    parser.parse(ast, location)
  }

  case class Parser(var nodes: Map[String, AmfElement]) {

    def parse(ast: AMFAST, location: String): BaseUnit = {
      val root = ast > SequenceToken > MapToken
      val ctx  = context(root)
      parse(root, ctx).set(Location, location).asInstanceOf[BaseUnit]
    }

    private def retrieveType(ast: AMFAST, ctx: GraphContext): Obj =
      ts(ast).find( t => {
        types.get(ctx.expand(t)).isDefined
      }) match {
        case Some(t) => types(ctx.expand(t))
        case None    => {
          throw new Exception(s"Error parsing JSON-LD node, unknown @types ${ts(ast)}")
        }
      }

    private def parse(node: AMFAST, ctx: GraphContext): AmfObject = {
      val id      = retrieveId(node)
      val sources = retrieveSources(id, node)
      val model   = retrieveType(node, ctx)

      val instance = builders(model)(annotations(sources, id))
      instance.withId(id)
      val children = node.children

      model.fields.foreach(f => {
        val k = ctx.reduce(f.value)
        children.find(key(k)) match {
          case Some(entry) => traverse(ctx, instance, f, value(f.`type`, entry.last), sources, k)
          case _           =>
        }
      })

      nodes = nodes + (id -> instance)
      instance
    }

    private def value(t: Type, node: AMFAST): AMFAST = {
      node.`type` match {
        case SequenceToken =>
          t match {
            case Array(_) => node
            case _        => value(t, node.head)
          }
        case MapToken =>
          t match {
            case Iri                            => node.children.find(key("@id")).get.last
            case Str | RegExp | Bool | Type.Int => node.children.find(key("@value")).get.last
            case _                              => node
          }
        case _ => node
      }
    }

    private def traverse(ctx: GraphContext,
                         instance: AmfObject,
                         f: Field,
                         node: AMFAST,
                         sources: SourceMap,
                         key: String) = {
      f.`type` match {
        case _: Obj             => instance.set(f, parse(node, ctx), annotations(sources, key))
        case Str | RegExp | Iri => instance.set(f, str(node), annotations(sources, key))
        case Bool               => instance.set(f, bool(node), annotations(sources, key))
        case Type.Int           => instance.set(f, int(node), annotations(sources, key))
        case a: Array =>
          val values: Seq[AmfElement] = a.element match {
            case _: Obj    => node.children.map(n => parse(n, ctx))
            case Str | Iri => node.children.map(n => str(value(a.element, n)))
          }
          instance.setArray(f, values, annotations(sources, key))
      }
    }

    private def context(ast: AMFAST): GraphContext = {
      ast.children.find(key("@context")) match {
        case Some(t) =>
          MapGraphContext(
            (t > MapToken).children
              .map((entry) => {
                entry.head.content.unquote -> Namespace(entry.last.content.unquote)
              })
              .toMap)
        case _ => EmptyGraphContext
      }
    }

    private def retrieveSources(id: String, ast: AMFAST): SourceMap = {
      ast.children.find(key(Sources.value.iri())) match {
        case Some(entry) => parseSourceNode(value(SourceMapModel, entry.last))
        case _           => SourceMap.empty
      }
    }

    private def parseSourceNode(node: AMFAST): SourceMap = {
      val result = SourceMap()
      node.children.foreach(entry => {
        entry.head.content.unquote match {
          case AnnotationName(annotation) =>
            val consumer = result.annotation(annotation)
            entry.last.children.foreach(node => {
              consumer(value(Value.`type`, node.head.last).content.unquote,
                       value(Element.`type`, node.last.last).content.unquote)
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

    private def retrieveId(ast: AMFAST): String = {
      ast.children.find(key("@id")) match {
        case Some(entry) => entry.last.content.unquote
        case _           => throw new Exception(s"No @id declaration on node $ast")
      }
    }

    private def ts(ast: AMFAST): Seq[String] = {
      ast.children.find(key("@type")) match {
        case Some(entry) => (entry > SequenceToken).children.map(_.content.unquote)
        case _           => throw new Exception(s"No @type declaration on node $ast")
      }
    }

    /** Find entry with matching key. */
    private def key(key: String)(n: AMFAST): Boolean = (n is Entry) && (n > StringToken) ? key

    private object AnnotationName {
      def unapply(uri: String): Option[String] = uri match {
        case url if url.startsWith(SourceMaps.base) => Some(url.substring(url.indexOf("#") + 1))
        case _                                      => None
      }
    }
  }

  private def str(node: AMFAST) = AmfScalar(node.content.unquote)

  private def bool(node: AMFAST) = AmfScalar(node.content.unquote.toBoolean)

  private def int(node: AMFAST) = AmfScalar(node.content.unquote.toInt)

  /** Object Type builders. */
  private val builders: Map[Obj, (Annotations) => AmfObject] = Map(
    DocumentModel      -> Document.apply,
    WebApiModel        -> WebApi.apply,
    OrganizationModel  -> Organization.apply,
    LicenseModel       -> License.apply,
    CreativeWorkModel  -> CreativeWork.apply,
    EndPointModel      -> EndPoint.apply,
    OperationModel     -> Operation.apply,
    ParameterModel     -> Parameter.apply,
    PayloadModel       -> Payload.apply,
    RequestModel       -> Request.apply,
    ResponseModel      -> Response.apply,
    NodeShapeModel     -> NodeShape.apply,
    ArrayShapeModel    -> ArrayShape.apply,
    ScalarShapeModel   -> ScalarShape.apply,
    PropertyShapeModel -> PropertyShape.apply,
    XMLSerializerModel -> XMLSerializer.apply,
    PropertyDependenciesModel -> PropertyDependencies.apply
  )

  private val types: Map[String, Obj] = builders.keys.map(t => t.`type`.head.iri() -> t).toMap
}
