package amf.apicontract.internal.spec.common.reference

import amf.apicontract.internal.spec.raml.RamlHeader.{Raml10Extension, Raml10Overlay}
import amf.apicontract.internal.spec.raml.parser.document.LibraryLocationParser
import amf.apicontract.internal.spec.raml.{RamlHeader, parser}
import amf.apicontract.internal.validation.definitions.ParserSideValidations._
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document._
import amf.core.internal.parser.YMapOps
import amf.core.internal.remote._
import amf.core.internal.utils.MediaTypeMatcher
import org.yaml.model._

import scala.util.matching.Regex

class ApiReferenceHandler(vendor: String) extends ReferenceHandler {

  private val references = CompilerReferenceCollector()

  override def collect(parsed: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    collect(parsed)(ctx.eh)
  }

  private def collect(parsed: ParsedDocument)(implicit errorHandler: AMFErrorHandler): CompilerReferenceCollector = {
    val doc = parsed.asInstanceOf[SyamlParsedDocument].document
    libraries(doc)
    links(doc)
    if (isRamlOverlayOrExtension(vendor, parsed)) overlaysAndExtensions(doc)
    references
  }

  // TODO take this away when dialects don't use 'extends' keyword.
  def isRamlOverlayOrExtension(vendor: String, parsed: ParsedDocument): Boolean = {
    parsed.asInstanceOf[SyamlParsedDocument].comment match {
      case Some(c) =>
        RamlHeader.fromText(c) match {
          case Some(Raml10Overlay | Raml10Extension) if vendor == Raml10.name => true
          case _                                                              => false
        }
      case None => false
    }
  }

  private def overlaysAndExtensions(document: YDocument)(implicit errorHandler: AMFErrorHandler): Unit = {
    document.node.to[YMap] match {
      case Right(map) =>
        val ext = vendor match {
          case Raml10.name             => Some("extends")
          case Oas20.name | Oas30.name => Some("x-extends")
          case _                       => None
        }

        ext.foreach { u =>
          map
            .key(u)
            .foreach(entry =>
              entry.value.tagType match {
                case YType.Map | YType.Seq | YType.Null =>
                  errorHandler
                    .violation(InvalidExtensionsType, "", s"Expected scalar but found: ${entry.value}", entry.value)
                case _ => extension(entry) // assume scalar
            })
        }
      case _ =>
    }
  }

  private def extension(entry: YMapEntry)(implicit errorHandler: AMFErrorHandler): Unit = {
    references += (entry.value.as[YScalar].text, ExtensionReference, entry.value)
  }

  private def links(part: YPart)(implicit errorHandler: AMFErrorHandler): Unit = {
    vendor match {
      case Raml10.name | Raml08.name => ramlLinks(part)
      case Oas20.name | Oas30.name   => oasLinks(part)
      case AsyncApi20.name =>
        oasLinks(part)
        ramlLinks(part)
    }
  }

  private def libraries(document: YDocument)(implicit errorHandler: AMFErrorHandler): Unit = {
    document.to[YMap] match {
      case Right(map) =>
        val uses = vendor match {
          case Raml10.name             => Some("uses")
          case Oas20.name | Oas30.name => Some("x-amf-uses")
          case _                       => None
        }
        uses.foreach(u => {
          map
            .key(u)
            .foreach(entry => {
              entry.value.tagType match {
                case YType.Map  => entry.value.as[YMap].entries.foreach(library(_))
                case YType.Null =>
                case _ =>
                  errorHandler.violation(InvalidModuleType, "", s"Expected map but found: ${entry.value}", entry.value)
              }
            })
        })
      case _ =>
    }
  }

  private def library(entry: YMapEntry)(implicit errorHandler: AMFErrorHandler): Unit =
    LibraryLocationParser(entry) match {
      case Some(location) => references += (location, LibraryReference, entry.value)
      case _              => errorHandler.violation(ModuleNotFound, "", "Missing library location", entry)
    }

  private def oasLinks(part: YPart)(implicit errorHandler: AMFErrorHandler): Unit = {
    part match {
      case map: YMap if map.entries.size == 1 && isRef(map.entries.head) => oasInclude(map)
      case _                                                             => part.children.foreach(c => oasLinks(c))
    }
  }

  private def oasInclude(map: YMap)(implicit errorHandler: AMFErrorHandler): Unit = {
    val ref = map.entries.head
    ref.value.tagType match {
      case YType.Str =>
        references += (ref.value
          .as[String], LinkReference, ref.value) // this is not for all scalar, link must be a string
      case _ => errorHandler.violation(UnexpectedReference, "", s"Unexpected $$ref with $ref", ref.value)
    }
  }

  private def isRef(entry: YMapEntry) = {
    entry.key.value match {
      case scalar: YScalar => scalar.text == "$ref"
      case _               => false
    }
  }

  private def ramlLinks(part: YPart)(implicit errorHandler: AMFErrorHandler): Unit = {
    part match {
      case node: YNode if node.tagType == YType.Include         => ramlInclude(node)
      case scalar: YScalar if scalar.value.isInstanceOf[String] => checkInlined(scalar)
      case _                                                    => part.children.foreach(ramlLinks(_))
    }
  }

  private val linkRegex: Regex = "(\"\\$ref\":\\s*\".*\")".r

  private def checkInlined(scalar: YScalar): Unit = {
    val str = scalar.value.asInstanceOf[String]
    if (str.isJson) {
      linkRegex.findAllIn(str).foreach { m =>
        try {
          val link = m.split("\"").last.split("#").head
          if (!link.contains("<<") && !link.contains(">>")) // no trait variables inside path
            references += (link, InferredLinkReference, YNode(scalar, YType.Str))
        } catch {
          case _: Exception => // don't stop the parsing
        }
      }
    }
  }

  private def ramlInclude(node: YNode)(implicit errorHandler: AMFErrorHandler): Unit = {
    node.value match {
      case scalar: YScalar =>
        references += (scalar.text, LinkReference, node)
      case _ => errorHandler.violation(UnexpectedReference, "", s"Unexpected !include with ${node.value}", node)
    }
  }
}