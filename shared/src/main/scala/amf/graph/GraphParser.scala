package amf.graph

import amf.builder._
import amf.common.AMFAST
import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.common.Strings.strings
import amf.document.BaseUnit
import amf.domain.Annotation
import amf.metadata.SourceMapModel.{Element, Value}
import amf.metadata.Type.{Array, Bool, Iri, RegExp, Str}
import amf.metadata.document.BaseUnitModel.Location
import amf.metadata.document.DocumentModel
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.domain._
import amf.metadata.{Field, Obj, SourceMapModel, Type}
import amf.model.{AmfElement, AmfObject}
import amf.vocabulary.Namespace
import amf.vocabulary.Namespace.SourceMaps

import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * AMF Graph parser
  */
object GraphParser {

  def parse(ast: AMFAST, location: String): BaseUnit = {
    val parser = Parser(Map())
    parser.parse(ast, location)

  }

  case class Parser(var elements: Map[String, AmfElement]) {

    def parse(ast: AMFAST, location: String): BaseUnit = {
      val root = ast > SequenceToken > MapToken
      val ctx  = context(root)
      build(parse(root, ctx).set(Location, location)).asInstanceOf[BaseUnit]
    }

    private def build(builder: Builder) = {
      val element: AmfObject = builder.build.asInstanceOf[AmfObject]
      elements = elements + (element.id -> element)
      element
    }

    private def retrieveType(ast: AMFAST, ctx: GraphContext): Obj = types(ctx.expand(ts(ast).head))

    private def parse(node: AMFAST, ctx: GraphContext): Builder = {
      val id      = retrieveId(node)
      val sources = retrieveSources(id, node)
      val model   = retrieveType(node, ctx)

      val builder = builders(model)(annotations(sources, id))
      builder.withId(id)
      val children = node.children

      model.fields.foreach(f => {
        val k = ctx.reduce(f.value)
        children.find(key(k)) match {
          case Some(entry) => traverse(ctx, builder, f, value(f.`type`, entry.last), sources, k)
          case _           =>
        }
      })

      builder
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
            case Iri                 => node.children.find(key("@id")).get.last
            case Str | RegExp | Bool => node.children.find(key("@value")).get.last
            case _                   => node
          }
        case _ => node
      }
    }

    private def traverse(ctx: GraphContext,
                         builder: Builder,
                         f: Field,
                         node: AMFAST,
                         sources: SourceMap,
                         key: String) = {
      f.`type` match {
        case _: Obj             => builder.set(f, build(parse(node, ctx)))
        case Str | RegExp | Iri => builder.set(f, node.content.unquote, annotations(sources, key))
        case Bool               => builder.set(f, node.content.toBoolean)
        case a: Array =>
          val values = a.element match {
            case _: Obj => node.children.map(n => build(parse(n, ctx)))
            case Str    => node.children.map(n => value(a.element, n).content.unquote)
          }
          builder.set(f, values)
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

    private def annotations(sources: SourceMap, key: String): List[Annotation] = {
      if (sources.nonEmpty) {

        implicit val objects: Map[String, AmfElement] = Map()

        val result = ListBuffer[Annotation]()
        sources.annotations.foreach {
          case (annotation, values: mutable.Map[String, String]) =>
            annotation match {
              case Annotation(deserialize) if values.contains(key) => result += deserialize(values(key), elements)
              case _                                               =>
            }
        }
        result.toList
      } else {
        Nil
      }
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

  /** Object Type builders. */
  private val builders: Map[Obj, (List[Annotation]) => Builder] = Map(
    DocumentModel     -> DocumentBuilder.apply,
    WebApiModel       -> WebApiBuilder.apply,
    OrganizationModel -> OrganizationBuilder.apply,
    LicenseModel      -> LicenseBuilder.apply,
    CreativeWorkModel -> CreativeWorkBuilder.apply,
    EndPointModel     -> EndPointBuilder.apply,
    OperationModel    -> OperationBuilder.apply
  )

  private val types: Map[String, Obj] = builders.keys.map(t => t.`type`.head.iri() -> t).toMap
}
