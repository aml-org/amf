package amf.plugins.document.graph.parser

import amf.core.metamodel.Type
import amf.core.metamodel.Type._
import amf.core.metamodel.document.SourceMapModel
import amf.core.metamodel.domain.DomainElementModel
import amf.core.model.document.SourceMap
import amf.core.model.domain.{AmfElement, Annotation}
import amf.core.parser.{Annotations, _}
import amf.core.vocabulary.Namespace
import amf.core.vocabulary.Namespace.SourceMaps
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
              val k       = element.key(SourceMapModel.Element.value.iri()).get
              val v       = element.key(SourceMapModel.Value.value.iri()).get
              consumer(value(SourceMapModel.Element.`type`, k.value).as[YScalar].text,
                       value(SourceMapModel.Value.`type`, v.value).as[YScalar].text)
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
        ctx.violation(s"No @id declaration on node $map", map)
        None
    }
  }

  protected def retrieveSources(id: String, map: YMap): SourceMap = {
    map
      .key(DomainElementModel.Sources.value.iri())
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
          case Iri                                       => m.key("@id").get.value
          case Str | RegExp | Bool | Type.Int | Type.Any => m.key("@value").get.value
          case _                                         => node
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
            case Annotation(deserialize) if values.contains(key) =>
              deserialize(values(key), nodes).foreach(result += _)
            case _ =>
          }
      }
    }

    result
  }

}
