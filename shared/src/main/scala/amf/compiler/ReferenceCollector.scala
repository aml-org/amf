package amf.compiler

import amf.dialects.DialectRegistry
import amf.document.BaseUnit
import amf.parser.YMapOps
import amf.remote._
import amf.spec.ParserContext
import amf.validation.Validation
import org.yaml.model._

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
  * Reference collector. Ideally references should be collected while parsing to avoid an unnecessary iteration.
  */
class ReferenceCollector(document: YDocument, vendor: Vendor, validation: Validation) {

  implicit val ctx: ParserContext = ParserContext(validation, vendor)

  private val references = new ArrayBuffer[Reference]

  def traverse(isRamlOverlayOrExtension: Boolean): Seq[Reference] = {
    if (vendor != Amf) {
      libraries(document)
      links(document)
      if (isRamlOverlayOrExtension) overlaysAndExtensions(document)
    }
    references
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

case class Reference(url: String, kind: Kind, ast: YAggregate) {

  def isRemote: Boolean = !url.startsWith("#")

  def resolve(remote: Platform,
              context: Context,
              cache: Cache,
              hint: Hint,
              currentValidation: Validation,
              dialectRegistry: DialectRegistry = DialectRegistry.default)(implicit ctx: ParserContext): Future[BaseUnit] = {
    AMFCompiler(url, remote, hint + kind, currentValidation, Some(context), Some(cache), dialectRegistry)(ctx)
      .build()
      .map(root => {
//        target = root
        root
      })
  }
}
