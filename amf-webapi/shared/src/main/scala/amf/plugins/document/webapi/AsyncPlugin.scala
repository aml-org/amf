package amf.plugins.document.webapi

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.client.remod.amfcore.config.RenderOptions
import amf.client.remod.amfcore.resolution.{PipelineInfo, PipelineName}
import amf.core.errorhandling.ErrorHandler
import amf.core.exception.InvalidDocumentHeaderException
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{EmptyFutureDeclarations, ParsedReference, ParserContext}
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.validation.core.ValidationProfile
import amf.plugins.document.webapi.contexts.emitter.async.{Async20SpecEmitterContext, AsyncSpecEmitterContext}
import amf.plugins.document.webapi.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.webapi.parser.AsyncHeader
import amf.plugins.document.webapi.parser.AsyncHeader.Async20Header
import amf.plugins.document.webapi.parser.spec.AsyncWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.async.{AsyncApi20DocumentEmitter, AsyncApi20DocumentParser}
import amf.plugins.document.webapi.resolution.pipelines.{Async20EditingPipeline, Async20ResolutionPipeline}
import amf.plugins.domain.webapi.models.api.Api
import amf.{Async20Profile, AsyncProfile, ProfileName}
import org.yaml.model.YDocument

sealed trait AsyncPlugin extends OasLikePlugin with CrossSpecRestriction {

  override val vendors: Seq[String] = Seq(vendor.name, AsyncApi.name)

  override def specContext(options: RenderOptions, errorHandler: ErrorHandler): AsyncSpecEmitterContext

  def context(loc: String,
              refs: Seq[ParsedReference],
              options: ParsingOptions,
              wrapped: ParserContext,
              ds: Option[AsyncWebApiDeclarations] = None): AsyncWebApiContext

  override def parse(document: Root, parentContext: ParserContext, options: ParsingOptions): BaseUnit = {
    implicit val ctx: AsyncWebApiContext = context(document.location, document.references, options, parentContext)
    restrictCrossSpecReferences(document, ctx)
    val parsed = parseAsyncUnit(document)
    promoteFragments(parsed, ctx)
  }

  private def parseAsyncUnit(root: Root)(implicit ctx: AsyncWebApiContext): BaseUnit = {
    AsyncHeader(root) match {
      case Some(Async20Header) => AsyncApi20DocumentParser(root).parseDocument()
//    case f             => AsyncFragmentParser(root, Some(f)).parseFragment()
      case _ => // unreachable as it is covered in canParse()
        throw new InvalidDocumentHeaderException(vendor.name)
    }
  }

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq(
    "application/json",
    "application/yaml",
    "application/x-yaml",
    "text/yaml",
    "text/vnd.yaml",
    "text/x-yaml",
    "application/asyncapi+json",
    "application/async+json",
    "application/asyncapi+yaml",
    "application/async+yaml",
    "application/asyncapi",
    "application/async"
  )
}

object Async20Plugin extends AsyncPlugin {

  override def specContext(options: RenderOptions, errorHandler: ErrorHandler): AsyncSpecEmitterContext =
    new Async20SpecEmitterContext(errorHandler)

  override protected def vendor: Vendor = AsyncApi20

  override def validVendorsToReference: Seq[String] = super.validVendorsToReference :+ Raml10.name

  override val validationProfile: ProfileName = Async20Profile

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = AsyncHeader(root).contains(Async20Header)

  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case document: Document => document.encodes.isInstanceOf[Api]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => false
        case _                => false
      }
    case _: Fragment => false
    case _           => false
  }

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: ErrorHandler): Option[YDocument] =
    unit match {

      case document: Document =>
        Some(new AsyncApi20DocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
      case _ => None
    }

  override val pipelines: Map[String, ResolutionPipeline] = Map(
    PipelineName.from(vendor.name, ResolutionPipeline.DEFAULT_PIPELINE) -> new Async20ResolutionPipeline(),
    PipelineName.from(vendor.name, ResolutionPipeline.EDITING_PIPELINE) -> new Async20EditingPipeline(),
    PipelineName.from(vendor.name, ResolutionPipeline.CACHE_PIPELINE)   -> new Async20EditingPipeline(false)
  )

  override def context(loc: String,
                       refs: Seq[ParsedReference],
                       options: ParsingOptions,
                       wrapped: ParserContext,
                       ds: Option[AsyncWebApiDeclarations]) = {
    // ensure unresolved references in external fragments are not resolved with main api definitions
    val cleanContext = wrapped.copy(futureDeclarations = EmptyFutureDeclarations())
    cleanContext.globalSpace = wrapped.globalSpace
    new Async20WebApiContext(loc, refs, cleanContext, ds, options = options)
  }

  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    super.domainValidationProfiles(platform).filterKeys(k => k == Async20Profile.p || k == AsyncProfile.p)
}
