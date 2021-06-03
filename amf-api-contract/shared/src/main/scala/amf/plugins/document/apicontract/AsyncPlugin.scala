package amf.plugins.document.apicontract

import amf.client.remod.amfcore.config.{ParsingOptions, RenderOptions}
import amf.core.Root
import amf.core.errorhandling.AMFErrorHandler
import amf.core.exception.InvalidDocumentHeaderException
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{EmptyFutureDeclarations, ParsedReference, ParserContext}
import amf.core.remote._
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.validation.core.ValidationProfile
import amf.plugins.document.apicontract.contexts.emitter.async.{Async20SpecEmitterContext, AsyncSpecEmitterContext}
import amf.plugins.document.apicontract.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.apicontract.parser.AsyncHeader
import amf.plugins.document.apicontract.parser.AsyncHeader.Async20Header
import amf.plugins.document.apicontract.parser.spec.AsyncWebApiDeclarations
import amf.plugins.document.apicontract.parser.spec.async.{AsyncApi20DocumentEmitter, AsyncApi20DocumentParser}
import amf.plugins.document.apicontract.resolution.pipelines.{
  Async20CachePipeline,
  Async20EditingPipeline,
  Async20TransformationPipeline
}
import amf.plugins.document.apicontract.validation.ApiValidationProfiles.Async20ValidationProfile
import amf.plugins.domain.apicontract.models.api.Api
import amf.{Async20Profile, ProfileName}
import org.yaml.model.YDocument

sealed trait AsyncPlugin extends OasLikePlugin with CrossSpecRestriction {

  override val vendors: Seq[String] = Seq(vendor.name, AsyncApi.name)

  override def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): AsyncSpecEmitterContext

  def context(loc: String,
              refs: Seq[ParsedReference],
              options: ParsingOptions,
              wrapped: ParserContext,
              ds: Option[AsyncWebApiDeclarations] = None): AsyncWebApiContext

  override def parse(document: Root, ctx: ParserContext): BaseUnit = {
    implicit val newCtx: AsyncWebApiContext = context(document.location, document.references, ctx.parsingOptions, ctx)
    restrictCrossSpecReferences(document, ctx)
    val parsed = parseAsyncUnit(document)
    promoteFragments(parsed, newCtx)
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

  override def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): AsyncSpecEmitterContext =
    new Async20SpecEmitterContext(errorHandler)

  override val vendors = Seq("application/asyncapi20", "application/asyncapi20+json", "application/asyncapi20+yaml")

  override def validVendorsToReference: Seq[String] =
    super.validVendorsToReference :+ "application/raml10+yaml" :+
      "application/raml10"

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
                                            errorHandler: AMFErrorHandler): Option[YDocument] =
    unit match {

      case document: Document =>
        Some(new AsyncApi20DocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
      case _ => None
    }

  override val pipelines: Map[String, TransformationPipeline] = Map(
    Async20TransformationPipeline.name -> Async20TransformationPipeline(),
    Async20EditingPipeline.name        -> Async20EditingPipeline(),
    Async20CachePipeline.name          -> Async20CachePipeline()
  )

  override def context(loc: String,
                       refs: Seq[ParsedReference],
                       options: ParsingOptions,
                       wrapped: ParserContext,
                       ds: Option[AsyncWebApiDeclarations]): Async20WebApiContext = {
    // ensure unresolved references in external fragments are not resolved with main api definitions
    val cleanContext = wrapped.copy(futureDeclarations = EmptyFutureDeclarations())
    cleanContext.globalSpace = wrapped.globalSpace
    new Async20WebApiContext(loc, refs, cleanContext, ds, options = options)
  }

  // TODO: Temporary, should be erased until synchronous validation profile building for dialects is implemented
  override def domainValidationProfiles: Seq[ValidationProfile] = Seq(Async20ValidationProfile)

  override protected def vendor: Vendor = AsyncApi20
}
