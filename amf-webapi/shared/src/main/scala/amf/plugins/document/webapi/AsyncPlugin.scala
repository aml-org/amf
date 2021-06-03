package amf.plugins.document.webapi

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
import amf.plugins.document.webapi.contexts.emitter.async.{Async20SpecEmitterContext, AsyncSpecEmitterContext}
import amf.plugins.document.webapi.contexts.parser.async.{Async20WebApiContext, AsyncWebApiContext}
import amf.plugins.document.webapi.parser.AsyncHeader
import amf.plugins.document.webapi.parser.AsyncHeader.Async20Header
import amf.plugins.document.webapi.parser.spec.AsyncWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.async.{AsyncApi20DocumentEmitter, AsyncApi20DocumentParser}
import amf.plugins.document.webapi.resolution.pipelines.{
  Async20CachePipeline,
  Async20EditingPipeline,
  Async20TransformationPipeline
}
import amf.plugins.document.webapi.validation.ApiValidationProfiles.Async20ValidationProfile
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.parse.Async20ParsePlugin
import amf.{Async20Profile, ProfileName}
import org.yaml.model.YDocument

sealed trait AsyncPlugin extends OasLikePlugin {

  override val vendors: Seq[String] = Seq(vendor.name, AsyncApi.name)

  override def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): AsyncSpecEmitterContext

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

  override val vendors = Async20ParsePlugin.mediaTypes

  override def validVendorsToReference: Seq[String] =
    super.validVendorsToReference :+ "application/raml10+yaml" :+
      "application/raml10"

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root, ctx: ParserContext): BaseUnit = Async20ParsePlugin.parse(document, ctx)

  override val validationProfile: ProfileName = Async20Profile

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = Async20ParsePlugin.applies(root)

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

  // TODO: Temporary, should be erased until synchronous validation profile building for dialects is implemented
  override def domainValidationProfiles: Seq[ValidationProfile] = Seq(Async20ValidationProfile)

  override protected def vendor: Vendor = Async20ParsePlugin.vendor
}
