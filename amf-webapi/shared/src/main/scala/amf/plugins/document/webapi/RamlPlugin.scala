package amf.plugins.document.webapi

import amf._
import amf.client.remod.amfcore.config.RenderOptions
import amf.core.Root
import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document._
import amf.core.parser.ParserContext
import amf.core.remote.Vendor
import amf.core.resolution.pipelines.TransformationPipeline
import amf.core.validation.core.ValidationProfile
import amf.plugins.document.webapi.contexts.emitter.raml.{
  Raml08SpecEmitterContext,
  Raml10SpecEmitterContext,
  RamlSpecEmitterContext
}
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.spec.raml.{RamlDocumentEmitter, RamlFragmentEmitter, RamlModuleEmitter}
import amf.plugins.document.webapi.references.RamlReferenceHandler
import amf.plugins.document.webapi.resolution.pipelines._
import amf.plugins.document.webapi.resolution.pipelines.compatibility.Raml10CompatibilityPipeline
import amf.plugins.document.webapi.validation.ApiValidationProfiles._
import amf.plugins.domain.webapi.models.api.{Api, WebApi}
import amf.plugins.parse.{Raml08ParsePlugin, Raml10ParsePlugin}
import org.yaml.model.{YDocument, YNode}

sealed trait RamlPlugin extends BaseWebApiPlugin {

  override def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): RamlSpecEmitterContext
}

object Raml08Plugin extends RamlPlugin {

  override def referenceHandler(eh: AMFErrorHandler) = new RamlReferenceHandler(Raml08ParsePlugin)

  override protected def vendor: Vendor = Raml08ParsePlugin.vendor

  override val validationProfile: ProfileName = Raml08Profile

  def canParse(root: Root): Boolean = Raml08ParsePlugin.applies(root)

  /**
    * Parses an accepted document returning an optional BaseUnit
    */
  override def parse(document: Root, ctx: ParserContext): BaseUnit = Raml08ParsePlugin.parse(document, ctx)

  // fix for 08
  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Overlay                           => false
    case _: Extension                         => false
    case document: Document                   => document.encodes.isInstanceOf[WebApi]
    case _: Module                            => false
    case _: DocumentationItemFragment         => true // remove raml header and write as external fragment
    case _: DataTypeFragment                  => true
    case _: NamedExampleFragment              => true
    case _: ResourceTypeFragment              => true
    case _: TraitFragment                     => true
    case _: AnnotationTypeDeclarationFragment => true
    case _: SecuritySchemeFragment            => true
    case _: ExternalFragment                  => true
    case _                                    => false
  }

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] =
    unit match {
      case document: Document =>
        Some(RamlDocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
      case fragment: Fragment =>
        Some(new RamlFragmentEmitter(fragment)(specContext(renderOptions, errorHandler)).emitFragment())
      case _ => None
    }

  def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): RamlSpecEmitterContext =
    new Raml08SpecEmitterContext(errorHandler)

  override val pipelines: Map[String, TransformationPipeline] = Map(
    Raml08TransformationPipeline.name -> Raml08TransformationPipeline(),
    Raml08EditingPipeline.name        -> Raml08EditingPipeline()
  )

  override def domainValidationProfiles: Seq[ValidationProfile] = Seq(Raml08ValidationProfile)

  override val vendors: Seq[String] = Raml08ParsePlugin.mediaTypes

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq(
    "application/raml08",
    "application/raml08+yaml"
  )
}

object Raml10Plugin extends RamlPlugin {

  override protected def vendor: Vendor = Raml10ParsePlugin.vendor

  override def referenceHandler(eh: AMFErrorHandler) = new RamlReferenceHandler(Raml10ParsePlugin)

  override val validationProfile: ProfileName = Raml10Profile

  def canParse(root: Root): Boolean = Raml10ParsePlugin.applies(root)

  override def parse(document: Root, ctx: ParserContext): BaseUnit = Raml10ParsePlugin.parse(document, ctx)

  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Overlay                           => true
    case _: Extension                         => true
    case document: Document                   => document.encodes.isInstanceOf[Api]
    case _: Module                            => true
    case _: DocumentationItemFragment         => true
    case _: DataTypeFragment                  => true
    case _: NamedExampleFragment              => true
    case _: ResourceTypeFragment              => true
    case _: TraitFragment                     => true
    case _: AnnotationTypeDeclarationFragment => true
    case _: SecuritySchemeFragment            => true
    case _: ExternalFragment                  => true
    case _                                    => false
  }

  override protected def unparseAsYDocument(unit: BaseUnit,
                                            renderOptions: RenderOptions,
                                            errorHandler: AMFErrorHandler): Option[YDocument] =
    unit match {
      case module: Module => Some(RamlModuleEmitter(module)(specContext(renderOptions, errorHandler)).emitModule())
      case document: Document =>
        Some(RamlDocumentEmitter(document)(specContext(renderOptions, errorHandler)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment =>
        Some(new RamlFragmentEmitter(fragment)(specContext(renderOptions, errorHandler)).emitFragment())
      case _ => None
    }

  def specContext(options: RenderOptions, errorHandler: AMFErrorHandler): RamlSpecEmitterContext =
    new Raml10SpecEmitterContext(errorHandler)

  override val pipelines: Map[String, TransformationPipeline] = Map(
    Raml10TransformationPipeline.name -> Raml10TransformationPipeline(),
    Raml10EditingPipeline.name        -> Raml10EditingPipeline(),
    Raml10CompatibilityPipeline.name  -> Raml10CompatibilityPipeline(),
    Raml10CachePipeline.name          -> Raml10CachePipeline()
  )

  override def domainValidationProfiles: Seq[ValidationProfile] = Seq(Raml10ValidationProfile, AmfValidationProfile)

  override val vendors: Seq[String] = Raml10ParsePlugin.mediaTypes

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq(
    "application/raml10",
    "application/raml10+yaml",
    "application/raml",
    "application/raml+yaml"
  )
}
