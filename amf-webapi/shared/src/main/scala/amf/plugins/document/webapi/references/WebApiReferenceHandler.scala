package amf.plugins.document.webapi.references

import amf.client.`new`.amfcore.AmfParsePlugin
import amf.core.CompilerContext
import amf.core.TaggedReferences._
import amf.core.annotations.SourceAST
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser._
import amf.core.parser.errorhandler.ParserErrorHandler
import amf.core.remote._
import amf.core.utils._
import amf.plugins.document.webapi.BaseWebApiPlugin
import amf.plugins.document.webapi.parser.RamlHeader
import amf.plugins.document.webapi.parser.RamlHeader.{Raml10Extension, Raml10Overlay}
import amf.plugins.document.webapi.parser.spec.declaration.LibraryLocationParser
import amf.validations.ParserSideValidations._
import org.yaml.model.YNode.MutRef
import org.yaml.model._
import org.yaml.parser.YamlParser

import scala.concurrent.{ExecutionContext, Future}
import scala.util.matching.Regex

class WebApiReferenceHandler(vendor: String, plugin: AmfParsePlugin) extends ReferenceHandler {

  private val references = CompilerReferenceCollector()

  override def collect(parsed: ParsedDocument, ctx: ParserContext): CompilerReferenceCollector = {
    collect(parsed)(ctx.eh)
  }

  private def collect(parsed: ParsedDocument)(implicit errorHandler: ParserErrorHandler): CompilerReferenceCollector = {
    val doc = parsed.asInstanceOf[SyamlParsedDocument].document
    libraries(doc)
    links(doc)
    if (isRamlOverlayOrExtension(vendor, parsed)) overlaysAndExtensions(doc)
    references
  }

  /** Update parsed reference if needed. */
  override def update(reference: ParsedReference, compilerContext: CompilerContext)(
      implicit executionContext: ExecutionContext): Future[ParsedReference] =
    vendor match {
      case Raml10.name | Raml08.name | Raml.name if reference.isExternalFragment =>
        handleRamlExternalFragment(reference, compilerContext)
      case _ => Future.successful(reference)
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

  private def overlaysAndExtensions(document: YDocument)(implicit errorHandler: ParserErrorHandler): Unit = {
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

  private def extension(entry: YMapEntry)(implicit errorHandler: ParserErrorHandler): Unit = {
    references += (entry.value.as[YScalar].text, ExtensionReference, entry.value)
  }

  private def links(part: YPart)(implicit errorHandler: ParserErrorHandler): Unit = {
    vendor match {
      case Raml10.name | Raml08.name | Raml.name => ramlLinks(part)
      case Oas20.name | Oas30.name               => oasLinks(part)
      case AsyncApi20.name =>
        oasLinks(part)
        ramlLinks(part)
    }
  }

  private def libraries(document: YDocument)(implicit errorHandler: ParserErrorHandler): Unit = {
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

  private def library(entry: YMapEntry)(implicit errorHandler: ParserErrorHandler): Unit =
    LibraryLocationParser(entry) match {
      case Some(location) => references += (location, LibraryReference, entry.value)
      case _              => errorHandler.violation(ModuleNotFound, "", "Missing library location", entry)
    }

  private def oasLinks(part: YPart)(implicit errorHandler: ParserErrorHandler): Unit = {
    part match {
      case map: YMap if map.entries.size == 1 && isRef(map.entries.head) => oasInclude(map)
      case _                                                             => part.children.foreach(c => oasLinks(c))
    }
  }

  private def oasInclude(map: YMap)(implicit errorHandler: ParserErrorHandler): Unit = {
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

  private def ramlLinks(part: YPart)(implicit errorHandler: ParserErrorHandler): Unit = {
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

  private def ramlInclude(node: YNode)(implicit errorHandler: ParserErrorHandler): Unit = {
    node.value match {
      case scalar: YScalar =>
        references += (scalar.text, LinkReference, node)
      case _ => errorHandler.violation(UnexpectedReference, "", s"Unexpected !include with ${node.value}", node)
    }
  }

  private def handleRamlExternalFragment(reference: ParsedReference, compilerContext: CompilerContext)(
      implicit executionContext: ExecutionContext): Future[ParsedReference] = {
    resolveUnitDocument(reference, compilerContext.parserContext) match {
      case Right(document) =>
        val parsed = SyamlParsedDocument(document)

        val refs    = new WebApiReferenceHandler(vendor, plugin).collect(parsed, compilerContext.parserContext)
        val updated = compilerContext.forReference(reference.unit.id, withNormalizedUri = false)

        val externals = refs.toReferences.map((r: Reference) => {
          r.resolve(updated, r.refs.map(_.node), allowRecursiveRefs = true, plugin) // why would this always allow recursions?
            .flatMap {
              case ReferenceResolutionResult(None, Some(unit)) =>
                val resolved = handleRamlExternalFragment(ParsedReference(unit, r), updated)
                reference.unit.tagReference(unit.location().getOrElse(unit.id), r)
                resolved.map(res => {
                  reference.unit.addReference(res.unit)
                  r.refs.foreach { refContainer =>
                    refContainer.node match {
                      case mut: MutRef =>
                        res.unit.references.foreach(u => compilerContext.parserContext.addSonRef(u))
                        mut.target = res.ast
                      case other =>
                        compilerContext.violation(InvalidFragmentType,
                                                  "Cannot inline a fragment in a not mutable node",
                                                  other)
                    }
                  // not meaning, only for collect all futures, not matter the type
                  }
                })
              case ReferenceResolutionResult(Some(_), _) => Future(Nil)
              case _                                     => Future(Nil)
            }
        })

        Future.sequence(externals).map(_ => reference.copy(ast = Some(document.node)))
      case Left(raw) =>
        Future.successful {
          reference.unit.references.foreach(u => compilerContext.parserContext.addSonRef(u))
          reference.copy(ast = Some(YNode(raw, reference.unit.location().getOrElse(""))))
        }
    }
  }

  private def isRamlOrYaml(encodes: ExternalDomainElement) =
    plugin.supportedMediatypes.contains(encodes.mediaType.value())

  private def resolveUnitDocument(reference: ParsedReference, ctx: ParserContext): Either[String, YDocument] = {
    reference.unit match {

      case e: ExternalFragment if isRamlOrYaml(e.encodes) =>
        Right(
          YamlParser(e.encodes.raw.value(), e.location().getOrElse(""))(ctx.eh)
            .withIncludeTag("!include")
            .document())
      case e: ExternalFragment =>
        Left(e.encodes.raw.value())
      case o if hasDocumentAST(o) =>
        Right(o.annotations.find(classOf[SourceAST]).map(_.ast.asInstanceOf[YDocument]).get)
      case _ => Left("")
    }
  }

  private def hasDocumentAST(other: BaseUnit) =
    other.annotations.find(classOf[SourceAST]).exists(_.ast.isInstanceOf[YDocument])
}
