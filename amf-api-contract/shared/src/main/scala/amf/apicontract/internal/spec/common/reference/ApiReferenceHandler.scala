package amf.apicontract.internal.spec.common.reference

import amf.apicontract.internal.spec.raml.RamlHeader
import amf.apicontract.internal.spec.raml.RamlHeader.{Raml10Extension, Raml10Overlay}
import amf.apicontract.internal.spec.raml.parser.document.LibraryLocationParser
import amf.apicontract.internal.validation.definitions.ParserSideValidations._
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.parse.document._
import amf.core.internal.parser.YMapOps
import amf.core.internal.remote._
import amf.core.internal.utils.MediaTypeMatcher
import amf.shapes.internal.reference.SYamlCompilerReferenceCollector
import org.yaml.model._

import scala.util.matching.Regex

class ApiReferenceHandler(spec: String) extends ReferenceHandler {

  private val references = SYamlCompilerReferenceCollector()

  override def collect(parsed: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    collect(parsed)(ctx.eh)
  }

  private def collect(parsed: ParsedDocument)(implicit errorHandler: AMFErrorHandler): CompilerReferenceCollector = {
    parsed match {
      case syaml: SyamlParsedDocument =>
        val doc = syaml.document
        libraries(doc)
        links(doc)
        if (isRamlOverlayOrExtension(spec, parsed)) overlaysAndExtensions(doc)
      case _ => // ignore @TODO: add proper reference collection here
    }
    references
  }

  // TODO take this away when dialects don't use 'extends' keyword.
  def isRamlOverlayOrExtension(spec: String, parsed: ParsedDocument): Boolean = {
    parsed.asInstanceOf[SyamlParsedDocument].comment match {
      case Some(c) =>
        RamlHeader.fromText(c) match {
          case Some(Raml10Overlay | Raml10Extension) if spec == Raml10.id => true
          case _                                                          => false
        }
      case None => false
    }
  }

  private def overlaysAndExtensions(document: YDocument)(implicit errorHandler: AMFErrorHandler): Unit = {
    document.node.to[YMap] match {
      case Right(map) =>
        val ext = spec match {
          case Raml10.id           => Some("extends")
          case Oas20.id | Oas30.id => Some("x-extends")
          case _                   => None
        }

        ext.foreach { u =>
          map
            .key(u)
            .foreach(entry =>
              entry.value.tagType match {
                case YType.Map | YType.Seq | YType.Null =>
                  errorHandler
                    .violation(
                      InvalidExtensionsType,
                      "",
                      s"Expected scalar but found: ${entry.value}",
                      entry.value.location
                    )
                case _ => extension(entry) // assume scalar
              }
            )
        }
      case _ =>
    }
  }

  private def extension(entry: YMapEntry)(implicit errorHandler: AMFErrorHandler): Unit = {
    references += (entry.value.as[YScalar].text, ExtensionReference, entry.value)
  }

  private def links(part: YPart)(implicit errorHandler: AMFErrorHandler): Unit = {
    spec match {
      case Raml10.id | Raml08.id => ramlLinks(part)
      case Oas20.id | Oas30.id   => oasLinks(part)
      case JsonSchema.id         => oasLinks(part)
      case AsyncApi20.id =>
        oasLinks(part)
        ramlLinks(part)
    }
  }

  private def libraries(document: YDocument)(implicit errorHandler: AMFErrorHandler): Unit = {
    document.to[YMap] match {
      case Right(map) =>
        val uses = spec match {
          case Raml10.id           => Some("uses")
          case Oas20.id | Oas30.id => Some("x-amf-uses")
          case _                   => None
        }
        uses.foreach(u => {
          map
            .key(u)
            .foreach(entry => {
              entry.value.tagType match {
                case YType.Map  => entry.value.as[YMap].entries.foreach(library(_))
                case YType.Null =>
                case _ =>
                  errorHandler
                    .violation(InvalidModuleType, "", s"Expected map but found: ${entry.value}", entry.value.location)
              }
            })
        })
      case _ =>
    }
  }

  private def library(entry: YMapEntry)(implicit errorHandler: AMFErrorHandler): Unit =
    LibraryLocationParser(entry) match {
      case Some(location) => references += (location, LibraryReference, entry.value)
      case _              => errorHandler.violation(ModuleNotFound, "", "Missing library location", entry.location)
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
      case _ => errorHandler.violation(UnexpectedReference, "", s"Unexpected $$ref with $ref", ref.value.location)
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
            references += (link, InferredLinkReference, scalar)
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
      case _ =>
        errorHandler.violation(UnexpectedReference, "", s"Unexpected !include with ${node.value}", node.location)
    }
  }
}
