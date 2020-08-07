package amf.plugins.document.webapi

import amf._
import amf.core.Root
import amf.core.client.ParsingOptions
import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{LibraryReference, LinkReference, ParsedReference, ParserContext}
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.validation.core.ValidationProfile
import amf.plugins.document.webapi.contexts.emitter.oas.{
  Oas2SpecEmitterContext,
  Oas3SpecEmitterContext,
  OasSpecEmitterContext
}
import amf.plugins.document.webapi.contexts.parser.oas.{Oas2WebApiContext, Oas3WebApiContext, OasWebApiContext}
import amf.plugins.document.webapi.model.{Extension, Overlay}
import amf.plugins.document.webapi.parser.OasHeader
import amf.plugins.document.webapi.parser.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay, Oas30Header}
import amf.plugins.document.webapi.parser.spec.OasWebApiDeclarations
import amf.plugins.document.webapi.parser.spec.oas._
import amf.plugins.document.webapi.resolution.pipelines.compatibility.CompatibilityPipeline
import amf.plugins.document.webapi.resolution.pipelines.{
  Oas30EditingPipeline,
  Oas30ResolutionPipeline,
  OasEditingPipeline,
  OasResolutionPipeline
}
import amf.plugins.domain.webapi.models.WebApi
import org.yaml.model.{YDocument, YNode}

sealed trait OasPlugin extends OasLikePlugin {

  override val vendors: Seq[String] = Seq(vendor.name, Oas.name)

  override def specContext(options: RenderOptions): OasSpecEmitterContext

  /**
    * Does references in this type of documents be recursive?
    */
  override val allowRecursiveReferences: Boolean = true

  def context(loc: String,
              refs: Seq[ParsedReference],
              options: ParsingOptions,
              wrapped: ParserContext,
              ds: Option[OasWebApiDeclarations] = None): OasWebApiContext

  override def parse(document: Root,
                     parentContext: ParserContext,
                     platform: Platform,
                     options: ParsingOptions): Option[BaseUnit] = {
    implicit val ctx: OasWebApiContext = context(document.location, document.references, options, parentContext)
    val parsed = document.referenceKind match {
      case LibraryReference => Some(OasModuleParser(document).parseModule())
      case LinkReference    => Some(OasFragmentParser(document).parseFragment())
      case _                => detectOasUnit(document)
    }
    parsed map { unit =>
      promoteFragments(unit, ctx)
    }
  }

  private def detectOasUnit(root: Root)(implicit ctx: OasWebApiContext): Option[BaseUnit] = {
    OasHeader(root) map {
      case Oas20Overlay   => Oas2DocumentParser(root).parseOverlay()
      case Oas20Extension => Oas2DocumentParser(root).parseExtension()
      case Oas20Header    => Oas2DocumentParser(root).parseDocument()
      case Oas30Header    => Oas3DocumentParser(root).parseDocument()
      case f              => OasFragmentParser(root, Some(f)).parseFragment()
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
    "application/openapi+json",
    "application/swagger+json",
    "application/openapi+yaml",
    "application/swagger+yaml",
    "application/openapi",
    "application/swagger"
  )
}

object Oas20Plugin extends OasPlugin {

  override def specContext(options: RenderOptions): OasSpecEmitterContext =
    new Oas2SpecEmitterContext(options.errorHandler)

  override protected def vendor: Vendor = Oas20

  override val validationProfile: ProfileName = Oas20Profile

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = OasHeader(root).exists(_ != Oas30Header)

  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Overlay         => true
    case _: Extension       => true
    case document: Document => document.encodes.isInstanceOf[WebApi]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => true
        case _                => false
      }
    case _: Fragment => true
    case _           => false
  }

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderOptions: RenderOptions,
      shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Option[YDocument] =
    unit match {
      case module: Module             => Some(Oas20ModuleEmitter(module)(specContext(renderOptions)).emitModule())
      case document: Document         => Some(Oas2DocumentEmitter(document)(specContext(renderOptions)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment         => Some(new OasFragmentEmitter(fragment)(specContext(renderOptions)).emitFragment())
      case _                          => None
    }

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit,
                       errorHandler: ErrorHandler,
                       pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit = pipelineId match {
    case ResolutionPipeline.DEFAULT_PIPELINE       => new OasResolutionPipeline(errorHandler).resolve(unit)
    case ResolutionPipeline.EDITING_PIPELINE       => new OasEditingPipeline(errorHandler).resolve(unit)
    case ResolutionPipeline.COMPATIBILITY_PIPELINE => new CompatibilityPipeline(errorHandler, OasProfile).resolve(unit)
    case ResolutionPipeline.CACHE_PIPELINE         => new OasEditingPipeline(errorHandler, false).resolve(unit)
    case _                                         => super.resolve(unit, errorHandler, pipelineId)
  }

  override def context(loc: String,
                       refs: Seq[ParsedReference],
                       options: ParsingOptions,
                       wrapped: ParserContext,
                       ds: Option[OasWebApiDeclarations]) = new Oas2WebApiContext(loc, refs, wrapped, ds, options)

  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    super.domainValidationProfiles(platform).filterKeys(k => k == Oas20Profile.p || k == OasProfile.p)
}

object Oas30Plugin extends OasPlugin {

  override def specContext(options: RenderOptions): Oas3SpecEmitterContext =
    new Oas3SpecEmitterContext(options.errorHandler)

  override protected def vendor: Vendor = Oas30

  override val validationProfile: ProfileName = Oas30Profile

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = OasHeader(root).contains(Oas30Header)

  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Overlay         => true
    case _: Extension       => true
    case document: Document => document.encodes.isInstanceOf[WebApi]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => true
        case _                => false
      }
    case _: Fragment => true
    case _           => false
  }

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderOptions: RenderOptions,
      shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Option[YDocument] =
    unit match {
      case module: Module             => Some(Oas30ModuleEmitter(module)(specContext(renderOptions)).emitModule())
      case document: Document         => Some(Oas3DocumentEmitter(document)(specContext(renderOptions)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment         => Some(new OasFragmentEmitter(fragment)(specContext(renderOptions)).emitFragment())
      case _                          => None
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
    "application/openapi+json",
    "application/swagger+json",
    "application/openapi+yaml",
    "application/swagger+yaml",
    "application/openapi",
    "application/swagger"
  )

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit,
                       errorHandler: ErrorHandler,
                       pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit = pipelineId match {
    case ResolutionPipeline.DEFAULT_PIPELINE => new Oas30ResolutionPipeline(errorHandler).resolve(unit)
    case ResolutionPipeline.EDITING_PIPELINE => new Oas30EditingPipeline(errorHandler).resolve(unit)
    case ResolutionPipeline.COMPATIBILITY_PIPELINE =>
      new CompatibilityPipeline(errorHandler, Oas30Profile).resolve(unit)
    case ResolutionPipeline.CACHE_PIPELINE => new Oas30EditingPipeline(errorHandler, false).resolve(unit)
    case _                                 => super.resolve(unit, errorHandler, pipelineId)
  }

  override def context(loc: String,
                       refs: Seq[ParsedReference],
                       options: ParsingOptions,
                       wrapped: ParserContext,
                       ds: Option[OasWebApiDeclarations]) = new Oas3WebApiContext(loc, refs, wrapped, ds, options)

  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    defaultValidationProfiles.filterKeys(_ == validationProfile.p)
}
