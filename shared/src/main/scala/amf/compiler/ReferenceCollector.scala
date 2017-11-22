package amf.compiler

import amf.core.{AMFCompiler => ReferenceCompiler}
import amf.document.BaseUnit
import amf.framework.parser.{Extension, Library, Link, ReferenceKind}
import amf.parser.YMapOps
import amf.plugins.document.webapi.parser.RamlHeader
import amf.plugins.document.webapi.parser.RamlHeader.{Raml10Extension, Raml10Overlay}
import amf.remote._
import amf.spec.ParserContext
import amf.validation.Validation
import org.yaml.model._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

abstract class AbstractReferenceCollector {
  def traverse(document: ParsedDocument, validation: Validation, ctx: ParserContext): Seq[Reference]
}

/**
  * Reference collector. Ideally references should be collected while parsing to avoid an unnecessary iteration.
  */
class ReferenceCollector(document: ParsedDocument, vendor: Vendor, validation: Validation) {

  implicit val ctx: ParserContext = ParserContext(validation)

  private val references = new ArrayBuffer[Reference]

  def traverse(): Seq[Reference] = {
    if (vendor != Amf) {
      libraries(document.document)
      links(document.document)
      if (isRamlOverlayOrExtension(vendor, document)) overlaysAndExtensions(document.document)
    }
    references
  }

  // TODO take this away when dialects don't use 'extends' keyword.
  def isRamlOverlayOrExtension(vendor: Vendor, document: ParsedDocument): Boolean = {
    document.comment match {
      case Some(c) =>
        RamlHeader.fromText(c.metaText) match {
          case Some(Raml10Overlay | Raml10Extension) if vendor == Raml => true
          case _                                                       => false
        }
      case None => false
    }
  }


  private def overlaysAndExtensions(document: YDocument): Unit = {
    document.node.to[YMap] match {
      case Right(map) =>
        val ext = vendor match {
          case Raml => Some("extends")
          case Oas  => Some("x-extends")
          case _    => None
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
    references += Reference(entry.value, Extension, entry)
  }

  private def links(part: YPart): Unit = {
    vendor match {
      case Raml => ramlLinks(part)
      case Oas  => oasLinks(part)
      case _    => // Ignore
    }
  }

  private def libraries(document: YDocument): Unit = {
    document.to[YMap] match {
      case Right(map) =>
        val uses = vendor match {
          case Raml => Some("uses")
          case Oas  => Some("x-uses")
          case _    => None
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

  private def library(entry: YMapEntry) = {
    references += Reference(entry.value, Library, entry)
  }

  def oasLinks(part: YPart): Unit = {
    part match {
      case map: YMap if map.entries.size == 1 && isRef(map.entries.head) => oasInclude(map)
      case _                                                             => part.children.foreach(oasLinks)
    }
  }

  private def oasInclude(map: YMap): Unit = {
    val ref = map.entries.head
    ref.value.tagType match {
      case YType.Str =>
        references += Reference(ref.value.as[String], Link, map) // this is not for all scalar, link must be a string
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
      case node: YNode if !node.tag.synthesized && node.tag.text == "!include" => ramlInclude(node)
      case _                                                                   => part.children.foreach(ramlLinks)
    }
  }

  private def ramlInclude(node: YNode) = {
    node.value match {
      case scalar: YScalar => references += Reference(scalar.text, Link, node)
      case _               => throw new Exception(s"Unexpected !include with ${node.value}")
    }
  }
}

case class Reference(url: String, kind: ReferenceKind, ast: YAggregate) {

  def isRemote: Boolean = !url.startsWith("#")

  def resolve(remote: Platform,
              base: Option[Context],
              mediaType: String,
              vendor: String,
              currentValidation: Validation,
              cache: Cache,
              dialects: amf.dialects.DialectRegistry,
              ctx: ParserContext): Future[BaseUnit] = {
    new ReferenceCompiler(url, remote, base, mediaType, vendor, kind, currentValidation, cache, Some(ctx))
      .build()
      .map(root => {
//        target = root
        root
      })
  }
}
