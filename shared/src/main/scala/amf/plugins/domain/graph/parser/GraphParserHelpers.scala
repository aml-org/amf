package amf.plugins.domain.graph.parser

import amf.domain.{Annotation, Annotations}
import amf.metadata.SourceMapModel.{Element, Value}
import amf.metadata.Type._
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.{SourceMapModel, Type}
import amf.model.AmfElement
import amf.parser.{YMapOps, YNodeLikeOps, YScalarYRead}
import amf.spec.ParserContext
import amf.vocabulary.Namespace
import amf.vocabulary.Namespace.SourceMaps
import org.yaml.convert.YRead.SeqNodeYRead
import org.yaml.model._

import scala.collection.mutable

trait GraphParserHelpers {

  private def parseSourceNode(map: YMap): SourceMap = {
    val result = SourceMap()
    map.entries.foreach(entry => {
      entry.key.toOption[YScalar].map(_.text).foreach {
        case AnnotationName(annotation) =>
          val consumer = result.annotation(annotation)
          entry.value
            .as[Seq[YNode]]
            .foreach(e => {
              val element = e.as[YMap]
              val k       = element.key(Element.value.iri()).get
              val v       = element.key(Value.value.iri()).get
              consumer(value(Element.`type`, k.value).as[YScalar].text, value(Value.`type`, v.value).as[YScalar].text)
            })
        case _ => // Unknown annotation identifier
      }
    })
    result
  }

  protected def ts(map: YMap, ctx: ParserContext, id: String): Seq[String] = {
    val documentType     = (Namespace.Document + "Document").iri()
    val fragmentType     = (Namespace.Document + "Fragment").iri()
    val moduleType       = (Namespace.Document + "Module").iri()
    val unitType         = (Namespace.Document + "Unit").iri()
    val documentTypesSet = Set(documentType, fragmentType, moduleType, unitType)
    map.key("@type") match {
      case Some(entry) =>
        val allTypes         = entry.value.toOption[Seq[YNode]].getOrElse(Nil).flatMap(v => v.toOption[YScalar].map(_.text))
        val nonDocumentTypes = allTypes.filter(t => !documentTypesSet.contains(t))
        val documentTypes    = allTypes.filter(t => documentTypesSet.contains(t)).sorted // we just use the fact that lexical order is correct
        nonDocumentTypes ++ documentTypes

      case _ =>
        ctx.violation(id, s"No @type declaration on node $map", map) // todo : review with pedro
        Nil
    }
  }

  protected def retrieveId(map: YMap, ctx: ParserContext): Option[String] = {
    map.key("@id") match {
      case Some(entry) => Some(entry.value.as[YScalar].text)
      case _ =>
        ctx.violation("", s"No @id declaration on node $map", map)
        None
    }
  }

  protected def retrieveSources(id: String, map: YMap): SourceMap = {
    map
      .key(Sources.value.iri())
      .flatMap { entry =>
        value(SourceMapModel, entry.value).toOption[YMap].map(parseSourceNode)
      }
      .getOrElse(SourceMap.empty)
  }

  protected def value(t: Type, node: YNode): YNode = {
    node.tagType match {
      case YType.Seq =>
        t match {
          case Array(_) => node
          case _        => value(t, node.as[Seq[YNode]].head)
        }
      case YType.Map =>
        val m: YMap = node.as[YMap]
        t match {
          case Iri                            => m.key("@id").get.value
          case Str | RegExp | Bool | Type.Int => m.key("@value").get.value
          case _                              => node
        }
      case _ => node
    }
  }

  protected object AnnotationName {
    def unapply(uri: String): Option[String] = uri match {
      case url if url.startsWith(SourceMaps.base) => Some(url.substring(url.indexOf("#") + 1))
      case _                                      => None
    }
  }

  protected def annotations(nodes: Map[String, AmfElement], sources: SourceMap, key: String): Annotations = {
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

}
