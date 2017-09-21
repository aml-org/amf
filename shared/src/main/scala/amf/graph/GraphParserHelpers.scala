package amf.graph

import amf.domain.{Annotation, Annotations}
import amf.metadata.SourceMapModel.{Element, Value}
import amf.metadata.Type._
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.{SourceMapModel, Type}
import amf.model.AmfElement
import amf.parser.{YMapOps, YValueOps}
import amf.vocabulary.Namespace.SourceMaps
import org.yaml.model.{YMap, YSequence, YValue}

import scala.collection.mutable

trait GraphParserHelpers {

  private def parseSourceNode(map: YMap): SourceMap = {
    val result = SourceMap()
    map.entries.foreach(entry => {
      entry.key.value.toScalar.text match {
        case AnnotationName(annotation) =>
          val consumer = result.annotation(annotation)
          entry.value.value.toSequence.values.foreach(e => {
            val element = e.toMap
            val k       = element.key(Element.value.iri()).get
            val v       = element.key(Value.value.iri()).get
            consumer(value(Element.`type`, k.value.value).toScalar.text,
                     value(Value.`type`, v.value.value).toScalar.text)
          })
        case _ => // Unknown annotation identifier
      }
    })
    result
  }

  protected def ts(map: YMap): Seq[String] = {
    map.key("@type") match {
      case Some(entry) => entry.value.value.toSequence.values.map(_.toScalar.text)
      case _           => throw new Exception(s"No @type declaration on node $map")
    }
  }

  protected def retrieveId(map: YMap): String = {
    map.key("@id") match {
      case Some(entry) => entry.value.value.toScalar.text
      case _           => throw new Exception(s"No @id declaration on node $map")
    }
  }

  protected def retrieveSources(id: String, map: YMap): SourceMap = {
    map.key(Sources.value.iri()) match {
      case Some(entry) => parseSourceNode(value(SourceMapModel, entry.value.value).toMap)
      case _           => SourceMap.empty
    }
  }

  protected def value(t: Type, node: YValue): YValue = {
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
