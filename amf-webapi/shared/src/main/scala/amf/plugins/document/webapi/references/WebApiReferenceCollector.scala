package amf.plugins.document.webapi.references

import amf.core.parser._
import amf.plugins.document.webapi.parser.RamlHeader
import amf.plugins.document.webapi.parser.RamlHeader.{Raml10Extension, Raml10Overlay}
import org.yaml.model._

import scala.collection.mutable.ArrayBuffer

class WebApiReferenceCollector(vendor: String) extends AbstractReferenceCollector {

  private val references = new ArrayBuffer[Reference]

  override def traverse(parsed: ParsedDocument, ctx: ParserContext): ArrayBuffer[Reference] = {
    libraries(parsed.document, ctx)
    links(parsed.document, ctx)
    if (isRamlOverlayOrExtension(vendor, parsed)) overlaysAndExtensions(parsed.document, ctx)
    references
  }

  // TODO take this away when dialects don't use 'extends' keyword.
  def isRamlOverlayOrExtension(vendor: String, parsed: ParsedDocument): Boolean = {
    parsed.comment match {
      case Some(c) =>
        RamlHeader.fromText(c.metaText) match {
          case Some(Raml10Overlay | Raml10Extension) if vendor == "RAML 1.0" => true
          case _                                                             => false
        }
      case None => false
    }
  }

  private def overlaysAndExtensions(document: YDocument, ctx: ParserContext): Unit = {
    document.node.to[YMap] match {
      case Right(map) =>
        val ext = vendor match {
          case "RAML 1.0" => Some("extends")
          case "OAS 2.0"  => Some("x-extends")
          case _          => None
        }

        ext.foreach { u =>
          map
            .key(u)
            .foreach(entry =>
              entry.value.tagType match {
                case YType.Map | YType.Seq =>
                  ctx.violation("", s"Expected scalar but found: ${entry.value}", entry.value)
                case _ => extension(entry) // assume scalar
            })
        }
      case _ =>
    }
  }

  private def extension(entry: YMapEntry) = {
    references += Reference(entry.value, ExtensionReference, entry.value)
  }

  private def links(part: YPart, ctx: ParserContext): Unit = {
    vendor match {
      case "RAML 1.0" => ramlLinks(part)
      case "OAS 2.0"  => oasLinks(part, ctx)
      case _          => // Ignore
    }
  }

  private def libraries(document: YDocument, ctx: ParserContext): Unit = {
    document.to[YMap] match {
      case Right(map) =>
        val uses = vendor match {
          case "RAML 1.0" => Some("uses")
          case "OAS 2.0"  => Some("x-uses")
          case _          => None
        }
        uses.foreach(u => {
          map
            .key(u)
            .foreach(entry => {
              entry.value.to[YMap] match {
                case Right(m) => m.entries.foreach(library)
                case _        => ctx.violation("", s"Expected map but found: ${entry.value}", entry.value)
              }
            })
        })
      case _ =>
    }
  }

  private def library(entry: YMapEntry) = references += Reference(libraryName(entry), LibraryReference, entry.value)

  private def libraryName(e: YMapEntry): String = e.value.tagType match {
    case YType.Include => e.value.as[YScalar].text
    case _             => e.value
  }

  def oasLinks(part: YPart, ctx: ParserContext): Unit = {
    part match {
      case map: YMap if map.entries.size == 1 && isRef(map.entries.head) => oasInclude(map, ctx)
      case _                                                             => part.children.foreach(c => oasLinks(c, ctx))
    }
  }

  private def oasInclude(map: YMap, ctx: ParserContext): Unit = {
    val ref = map.entries.head
    ref.value.tagType match {
      case YType.Str =>
        references += Reference(ref.value.as[String], LinkReference, ref.value) // this is not for all scalar, link must be a string
      case _ => ctx.violation("", s"Unexpected $$ref with $ref", ref.value)
    }
  }

  private def isRef(entry: YMapEntry) = {
    entry.key.value match {
      case scalar: YScalar => scalar.text == "$ref"
      case _               => false
    }
  }

  def ramlLinks(part: YPart): Unit = {
    part match {
      case node: YNode if node.tagType == YType.Include => ramlInclude(node)
      case _                                            => part.children.foreach(ramlLinks)
    }
  }

  private def ramlInclude(node: YNode) = {
    node.value match {
      case scalar: YScalar => references += Reference(scalar.text, LinkReference, node)
      case _               => throw new Exception(s"Unexpected !include with ${node.value}")
    }
  }
}
