package amf.plugins.domain.vocabularies

import amf.compiler.{AbstractReferenceCollector, Reference}
import amf.framework.parser.{Library, Link}
import amf.parser.YMapOps
import amf.spec.ParserContext
import amf.validation.Validation
import org.yaml.model._

import scala.collection.mutable.ArrayBuffer

class RAMLExtensionsReferenceCollector extends AbstractReferenceCollector {
  private val references = new ArrayBuffer[Reference]

  override def traverse(document: YDocument, validation: Validation, ctx: ParserContext) = {
    libraries(document, ctx)
    links(document)

    references
  }

  private def libraries(document: YDocument, ctx: ParserContext): Unit = {
    document.to[YMap] match {
      case Right(map) =>
        map
          .key("uses")
          .foreach(entry => {
            entry.value.to[YMap] match {
              case Right(m) => m.entries.foreach(library)
              case _        => ctx.violation("", s"Expected map but found: ${entry.value}", entry.value)
            }
          })
      case _ =>
    }
  }

  private def library(entry: YMapEntry) = {
    references += Reference(entry.value, Library, entry)
  }

  private def links(part: YPart): Unit = {
    part match {
      case node: YNode if !node.tag.synthesized && node.tag.text == "!include" => ramlInclude(node)
      case _                                                                   => part.children.foreach(links)
    }
  }

  private def ramlInclude(node: YNode) = {
    node.value match {
      case scalar: YScalar => references += Reference(scalar.text, Link, node)
      case _               => throw new Exception(s"Unexpected !include with ${node.value}")
    }
  }
}
