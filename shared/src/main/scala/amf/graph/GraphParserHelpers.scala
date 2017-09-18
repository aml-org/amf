package amf.graph

import amf.common.core._
import amf.common.AMFAST
import amf.common.AMFToken.{Entry, MapToken, SequenceToken, StringToken}
import amf.domain.{Annotation, Annotations}
import amf.metadata.{SourceMapModel, Type}
import amf.metadata.Type.{Array, Bool, Iri, RegExp, Str}
import amf.metadata.domain.DomainElementModel.Sources
import amf.metadata.SourceMapModel.{Element, Value}
import amf.model.AmfElement
import amf.vocabulary.Namespace.SourceMaps
import scala.collection.mutable

trait GraphParserHelpers {

  protected def retrieveId(ast: AMFAST): String = {
    ast.children.find(key("@id")) match {
      case Some(entry) => entry.last.content.unquote
      case _           => throw new Exception(s"No @id declaration on node $ast")
    }
  }

  protected def retrieveSources(id: String, ast: AMFAST): SourceMap = {
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

  protected def value(t: Type, node: AMFAST): AMFAST = {
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

  protected def ts(ast: AMFAST): Seq[String] = {
    ast.children.find(key("@type")) match {
      case Some(entry) => (entry > SequenceToken).children.map(_.content.unquote)
      case _           => throw new Exception(s"No @type declaration on node $ast")
    }
  }

  /** Find entry with matching key. */
  protected def key(key: String)(n: AMFAST): Boolean = (n is Entry) && (n > StringToken) ? key

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
