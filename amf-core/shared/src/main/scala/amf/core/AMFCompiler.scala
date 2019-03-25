package amf.core

import java.net.URISyntaxException

import amf.client.plugins.AMFDocumentPlugin
import amf.client.remote.Content
import amf.core
import amf.core.benchmark.ExecutionLog
import amf.core.client.ParsingOptions
import amf.core.exception.{CyclicReferenceException, UnsupportedMediaTypeException, UnsupportedVendorException}
import amf.core.model.document.{BaseUnit, ExternalFragment}
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser.{
  ParsedDocument,
  ParsedReference,
  ParserContext,
  RefContainer,
  ReferenceKind,
  ReferenceResolutionResult,
  UnspecifiedReference
}
import amf.core.registries.AMFPluginsRegistry
import amf.core.remote._
import amf.core.services.RuntimeCompiler
import amf.core.utils.Strings
import amf.internal.environment.Environment
import amf.plugins.features.validation.ParserSideValidations.{
  CycleReferenceError,
  InvalidCrossSpec,
  UnresolvedReference,
  UriSyntaxError,
  InvalidFragmentRef
}
import org.yaml.model.YNode

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.Future.failed

object AMFCompilerRunCount {
  var count = 0
  def nextRun(): Int = synchronized {
    count += 1
    count
  }
}

class AMFCompiler(val rawUrl: String,
                  val remote: Platform,
                  val base: Option[Context],
                  val mediaType: Option[String],
                  val vendor: Option[String],
                  val referenceKind: ReferenceKind = UnspecifiedReference,
                  private val cache: Cache,
                  private val baseContext: Option[ParserContext] = None,
                  val env: Environment = Environment(),
                  val parsingOptions: ParsingOptions = ParsingOptions()) {

  val path: String = {
    try {
      rawUrl.normalizePath
    } catch {
      case e: URISyntaxException =>
        baseContext
          .getOrElse(ParserContext(rawUrl))
          .violation(UriSyntaxError, path, e.getMessage, YNode(path))
        rawUrl
      case e: Exception => throw new PathResolutionError(e.getMessage)
    }
  }

  private val context: Context = base.map(_.update(path)).getOrElse(core.remote.Context(remote, path))
  private val location         = context.current
  private val ctx: ParserContext = baseContext match {
    case Some(given) if given.rootContextDocument.equals(location) => given
    case Some(given)                                               => given.forLocation(location)
    case None                                                      => ParserContext(location)
  }

  def build(): Future[BaseUnit] = {
    ExecutionLog.log(s"AMFCompiler#build: Building $rawUrl")
    if (context.hasCycles) failed(new CyclicReferenceException(context.history))
    else
      cache.getOrUpdate(location, context) { () =>
        ExecutionLog.log(s"AMFCompiler#build: compiling $rawUrl")
        compile()
      }
  }

  private def compile() = resolve().map(parseSyntax).flatMap(parseDomain)

  def autodetectSyntax(stream: CharSequence): Option[String] = {
    if (stream.length() > 2 && stream.charAt(0) == '#' && stream.charAt(1) == '%') {
      ExecutionLog.log(s"AMFCompiler#autodetectSyntax: auto detected application/yaml media type")
      Some("application/yaml")
    } else {
      base.flatMap { b =>
        b.platform.findCharInCharSequence(stream) { c =>
          c != '\n' && c != '\t' && c != '\r' && c != ' '
        } match {
          case Some(c) if c == '{' || c == '[' =>
            ExecutionLog.log(s"AMFCompiler#autodetectSyntax: auto detected application/json media type")
            Some("application/json")
          case _ => None
        }
      }
    }
  }

  private def parseSyntax(input: Content): Either[Content, Root] = {
    ExecutionLog.log(s"AMFCompiler#parseSyntax: parsing syntax $rawUrl")
    val content = AMFPluginsRegistry.featurePlugins().foldLeft(input) {
      case (c, p) =>
        p.onBeginDocumentParsing(path, c, referenceKind)
    }

    val parsed: Option[(String, ParsedDocument)] = mediaType
      .flatMap { mime =>
        AMFPluginsRegistry
          .syntaxPluginForMediaType(mime)
          .flatMap(_.parse(mime, content.stream, ctx, parsingOptions))
          .map((mime, _))
      }
      .orElse {
        mediaType match {
          case None =>
            content.mime
              .flatMap { mime =>
                AMFPluginsRegistry
                  .syntaxPluginForMediaType(mime)
                  .flatMap(_.parse(mime, content.stream, ctx, parsingOptions))
                  .map((mime, _))
              }
              .orElse {
                FileMediaType
                  .extension(content.url)
                  .flatMap(FileMediaType.mimeFromExtension)
                  .flatMap { infered =>
                    AMFPluginsRegistry
                      .syntaxPluginForMediaType(infered)
                      .flatMap(_.parse(infered, content.stream, ctx, parsingOptions))
                      .map((infered, _))
                  }
              }
              .orElse {
                autodetectSyntax(content.stream).flatMap { infered =>
                  AMFPluginsRegistry
                    .syntaxPluginForMediaType(infered)
                    .flatMap(_.parse(infered, content.stream, ctx, parsingOptions))
                    .map((infered, _))
                }
              }
          case _ => None
        }
      }

    parsed match {
      case Some((effective, document)) =>
        val doc = AMFPluginsRegistry.featurePlugins().foldLeft(document) {
          case (d, p) =>
            p.onSyntaxParsed(path, d)
        }
        Right(Root(doc, content.url, effective, Seq(), referenceKind, content.stream.toString))
      case None =>
        Left(content)
    }
  }

  def parseExternalFragment(content: Content): Future[BaseUnit] = Future {
    val result = ExternalDomainElement().withId(content.url + "#/").withRaw(content.stream.toString)
    content.mime.foreach(mime => result.withMediaType(mime))
    ExternalFragment().withLocation(content.url).withId(content.url).withEncodes(result).withLocation(content.url)
  }

  private def parseDomain(parsed: Either[Content, Root]): Future[BaseUnit] = {
    parsed match {
      case Left(content) =>
        mediaType match {
          // if is Left (empty or other error) and is root (context.history.length == 1), then return an error
          case Some(mime) if context.history.length == 1 => throw new UnsupportedMediaTypeException(mime)
          case _                                         => parseExternalFragment(content)
        }
      case Right(document) => parseDomain(document)
    }
  }

  private def parseDomain(document: Root): Future[BaseUnit] = {
    ExecutionLog.log(s"AMFCompiler#parseDomain: parsing domain $rawUrl")
    val currentRun = ctx.parserCount
    val domainPluginOption =
      vendor.fold(AMFPluginsRegistry.documentPluginForMediaType(document.mediatype).find(_.canParse(document)))({
        AMFPluginsRegistry.documentPluginForVendor(_).find(_.canParse(document))
      })

    val futureDocument: Future[BaseUnit] = domainPluginOption match {
      case Some(domainPlugin) =>
        ExecutionLog.log(s"AMFCompiler#parseSyntax: parsing domain $rawUrl plugin ${domainPlugin.ID}")
        parseReferences(document, domainPlugin) map { documentWithReferences =>
          val newCtx = ctx.copyWithSonsReferences()
          domainPlugin.parse(documentWithReferences, newCtx, remote, parsingOptions) match {
            case Some(baseUnit) =>
              baseUnit.withRaw(document.raw)
            case None =>
              ExternalFragment()
                .withId(document.location)
                .withLocation(document.location)
                .withEncodes(
                  ExternalDomainElement()
                    .withRaw(document.raw)
                    .withMediaType(document.mediatype))
          }
        }
      case None if vendor.isDefined => throw new UnsupportedVendorException(vendor.get)
      case None =>
        Future {
          ExecutionLog.log(s"AMFCompiler#parseSyntax: parsing domain $rawUrl NO PLUGIN")
          ExternalFragment()
            .withLocation(document.location)
            .withId(document.location)
            .withEncodes(ExternalDomainElement().withRaw(document.raw).withMediaType(document.mediatype))
        }

    }

    futureDocument map { baseUnit: BaseUnit =>
      // we setup the run for the parsed unit
      baseUnit.parserRun = Some(currentRun)
      ExecutionLog.log(s"AMFCompiler#parseDomain: model ready $rawUrl")
      val bu = AMFPluginsRegistry.featurePlugins().foldLeft(baseUnit) {
        case (unit, plugin) =>
          plugin.onModelParsed(path, unit)
      }
      baseUnit
    }
  }

  private def parseReferences(root: Root, domainPlugin: AMFDocumentPlugin): Future[Root] = {
    val handler = domainPlugin.referenceHandler(ctx)
    val refs    = handler.collect(root.parsed, ctx)
    ExecutionLog.log(s"AMFCompiler#parseReferences: ${refs.toReferences.size} references found in $rawUrl")
    val parsed: Seq[Future[Option[ParsedReference]]] = refs.toReferences
      .filter(_.isRemote)
      .map { link =>
        val nodes = link.refs.map(_.node)
        link.resolve(context, cache, ctx, env, nodes, domainPlugin.allowRecursiveReferences) flatMap {
          case ReferenceResolutionResult(_, Some(unit)) =>
            verifyMatchingVendor(unit.sourceVendor, nodes)
            verifyValidFragment(unit.sourceVendor, link.refs)
            val reference = ParsedReference(unit, link)
            handler.update(reference, ctx, context, env, cache).map(Some(_))
          case ReferenceResolutionResult(Some(e), _) =>
            e match {
              case e: CyclicReferenceException if !domainPlugin.allowRecursiveReferences =>
                ctx
                  .violation(CycleReferenceError, link.url, e.getMessage, link.refs.head.node)
                Future(None)
              case _ =>
                if (!link.isInferred) {
                  nodes.foreach { ref =>
                    ctx.violation(UnresolvedReference, link.url, e.getMessage, ref)
                  }
                }
                Future(None)
            }
          case _ => Future(None)
        }
      }

    Future.sequence(parsed).map(rs => root.copy(references = rs.flatten))
  }

  private def resolve(): Future[Content] = remote.resolve(location, env)

  private def verifyMatchingVendor(refVendor: Option[Vendor], nodes: Seq[YNode]): Unit = refVendor match {
    case Some(v) if vendor.nonEmpty && !v.name.contains(vendor.get) =>
      nodes.foreach(ctx.violation(InvalidCrossSpec, "", "Cannot reference fragments of another spec", _))
    case _ => // Nothing to do
  }

  def verifyValidFragment(refVendor: Option[Vendor], refs: Seq[RefContainer]): Unit = refVendor match {
    case Some(v) if v.isRaml =>
      refs.foreach(
        r =>
          if (r.fragment.isDefined)
            ctx.violation(InvalidFragmentRef, "", "Cannot use reference with # in a RAML fragment", r.node))
    case _ => // Nothing to do
  }

  def root(): Future[Root] = resolve().map(parseSyntax).flatMap {
    case Right(document: Root) =>
      AMFPluginsRegistry.documentPluginForMediaType(document.mediatype).find(_.canParse(document)) match {
        case Some(domainPlugin) =>
          parseReferences(document, domainPlugin)
        case None =>
          Future {
            document
          }
      }
    case Left(content) =>
      throw new Exception(s"Cannot parse document with mime type ${content.mime.getOrElse("none")}")
  }
}

object AMFCompiler {
  def init() {
    // We register ourselves as the Runtime compiler
    if (RuntimeCompiler.compiler.isEmpty) {
      RuntimeCompiler.register(
        (url: String,
         base: Context,
         mediaType: Option[String],
         vendor: Option[String],
         referenceKind: ReferenceKind,
         cache: Cache,
         ctx: Option[ParserContext],
         env: Environment,
         parsingOptions: ParsingOptions) => {
          new AMFCompiler(url,
                          base.platform,
                          Some(base),
                          mediaType,
                          vendor,
                          referenceKind,
                          cache,
                          ctx,
                          env,
                          parsingOptions).build()
        })
    }
  }
}

case class Root(parsed: ParsedDocument,
                location: String,
                mediatype: String,
                references: Seq[ParsedReference],
                referenceKind: ReferenceKind,
                raw: String) {}
object Root {
  def apply(parsed: ParsedDocument,
            location: String,
            mediatype: String,
            references: Seq[ParsedReference],
            referenceKind: ReferenceKind,
            raw: String): Root =
    new Root(parsed, location.normalizeUrl, mediatype, references, referenceKind, raw)
}
