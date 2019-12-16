package amf.plugins.document.webapi

import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.model.document._
import amf.core.parser.{ErrorHandler, ParsedReference, ParserContext}
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.validation.core.ValidationProfile
import amf.plugins.document.webapi.contexts.emitter.async.{Async20SpecEmitterContext, AsyncSpecEmitterContext}
import amf.plugins.document.webapi.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.webapi.parser.AsyncHeader
import amf.plugins.document.webapi.parser.AsyncHeader.Async20Header
import amf.plugins.document.webapi.parser.spec.AsyncWebApiDeclarations
import amf.{Async20Profile, AsyncProfile, ProfileName}
import org.yaml.model.YDocument

sealed trait AsyncPlugin extends OasLikePlugin {

  override val vendors: Seq[String] = Seq(vendor.name, Oas.name)

  override def specContext(options: RenderOptions): AsyncSpecEmitterContext

  def context(loc: String,
              refs: Seq[ParsedReference],
              wrapped: ParserContext,
              ds: Option[AsyncWebApiDeclarations] = None): AsyncWebApiContext

  override def parse(document: Root,
                     parentContext: ParserContext,
                     platform: Platform,
                     options: ParsingOptions): Option[BaseUnit] = {
    implicit val ctx: AsyncWebApiContext = context(document.location, document.references, parentContext)
    val parsed = document.referenceKind match {
      case _ => detectAsyncUnit(document)
    }
    parsed map { unit =>
      promoteFragments(unit, ctx)
    }
    None
  }

  private def detectAsyncUnit(root: Root)(implicit ctx: AsyncWebApiContext): Option[BaseUnit] = {
//    AsyncHeader(root) map {
//      case Async20Header   => Async20DocumentParser(root).parse()
//      case f              => AsyncFragmentParser(root, Some(f)).parseFragment()
//    }
    None
    // TODO ASYNC update this
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

  override def specContext(options: RenderOptions): AsyncSpecEmitterContext =
    new Async20SpecEmitterContext(options.errorHandler)

  override protected def vendor: Vendor = AsyncApi20

  override val validationProfile: ProfileName = Async20Profile

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = AsyncHeader(root).exists(_ != Async20Header)

  override def canUnparse(unit: BaseUnit): Boolean = false

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderOptions: RenderOptions,
      shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Option[YDocument] = None

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit,
                       errorHandler: ErrorHandler,
                       pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit = null
//  pipelineId match {
//    case ResolutionPipeline.DEFAULT_PIPELINE       => new OasResolutionPipeline(errorHandler).resolve(unit)
//    case ResolutionPipeline.EDITING_PIPELINE       => new OasEditingPipeline(errorHandler).resolve(unit)
//    case ResolutionPipeline.COMPATIBILITY_PIPELINE => new CompatibilityPipeline(errorHandler, OasProfile).resolve(unit)
//    case ResolutionPipeline.CACHE_PIPELINE         => new OasEditingPipeline(errorHandler, false).resolve(unit)
//    case _                                         => super.resolve(unit, errorHandler, pipelineId)
//  }

  override def context(loc: String,
                       refs: Seq[ParsedReference],
                       wrapped: ParserContext,
                       ds: Option[AsyncWebApiDeclarations]) = new Async20WebApiContext(loc, refs, wrapped, ds)

  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    super.domainValidationProfiles(platform).filterKeys(k => k == Async20Profile.p || k == AsyncProfile.p)
}
