package amf.plugins.document.webapi

import amf._
import amf.core.client.ParsingOptions
import amf.core.emitter.{RenderOptions, ShapeRenderOptions}
import amf.core.errorhandling.ErrorHandler
import amf.core.model.document._
import amf.core.model.domain.ExternalDomainElement
import amf.core.parser.{
  EmptyFutureDeclarations,
  LibraryReference,
  LinkReference,
  ParsedReference,
  ParserContext,
  RefContainer,
  ReferenceKind,
  UnspecifiedReference
}
import amf.core.remote.{Platform, Raml, Vendor}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.validation.core.ValidationProfile
import amf.core.{CompilerContext, Root}
import amf.plugins.document.webapi.contexts.emitter.raml.{
  Raml08SpecEmitterContext,
  Raml10SpecEmitterContext,
  RamlSpecEmitterContext
}
import amf.plugins.document.webapi.contexts.parser.raml.{Raml08WebApiContext, Raml10WebApiContext, RamlWebApiContext}
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.RamlFragmentHeader._
import amf.plugins.document.webapi.parser.RamlHeader.{Raml10, Raml10Extension, Raml10Library, Raml10Overlay, _}
import amf.plugins.document.webapi.parser.spec.raml.{RamlDocumentEmitter, RamlFragmentEmitter, RamlModuleEmitter, _}
import amf.plugins.document.webapi.parser.spec.{RamlWebApiDeclarations, WebApiDeclarations}
import amf.plugins.document.webapi.parser.{RamlFragment, RamlHeader}
import amf.plugins.document.webapi.references.RamlReferenceHandler
import amf.plugins.document.webapi.resolution.pipelines.compatibility.CompatibilityPipeline
import amf.plugins.document.webapi.resolution.pipelines.{
  Raml08EditingPipeline,
  Raml08ResolutionPipeline,
  Raml10EditingPipeline,
  Raml10ResolutionPipeline
}
import amf.plugins.domain.webapi.models.api.{Api, WebApi}
import amf.plugins.features.validation.CoreValidations.{ExpectedModule, InvalidFragmentRef, InvalidInclude}
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YDocument, YNode}

sealed trait RamlPlugin extends BaseWebApiPlugin with CrossSpecRestriction {

  override val vendors: Seq[String] = Seq(vendor.name, Raml.name)

  override def referenceHandler(eh: ErrorHandler) = new RamlReferenceHandler(ID)

  def context(wrapped: ParserContext,
              root: Root,
              options: ParsingOptions,
              ds: Option[WebApiDeclarations] = None): RamlWebApiContext

  // context that opens a new context for declarations and copies the global JSON Schema declarations
  def cleanContext(wrapped: ParserContext, root: Root, options: ParsingOptions): RamlWebApiContext = {
    val cleanNested =
      ParserContext(root.location, root.references, EmptyFutureDeclarations(), wrapped.eh)
    val clean = context(cleanNested, root, options)
    clean.globalSpace = wrapped.globalSpace
    clean
  }

  override def specContext(options: RenderOptions): RamlSpecEmitterContext

  override def parse(root: Root, parentContext: ParserContext, options: ParsingOptions): Option[BaseUnit] = {

    val updated = context(parentContext, root, options)
    restrictCrossSpecReferences(root, updated)
    inlineExternalReferences(root, updated)
    val clean = cleanContext(parentContext, root, options)

    validateReferences(root.references, parentContext)
    RamlHeader(root) flatMap { // todo review this, should we use the raml web api context for get the version parser?

      // Partial raml0.8 fragment with RAML header but linked through !include
      // we need to generate an external fragment and inline it in the parent document
      case Raml08 if root.referenceKind == LinkReference => None

      case Raml08          => Some(Raml08DocumentParser(root)(updated).parseDocument())
      case Raml10          => Some(Raml10DocumentParser(root)(updated).parseDocument())
      case Raml10Overlay   => Some(ExtensionLikeParser.apply(root, updated).parseOverlay())
      case Raml10Extension => Some(ExtensionLikeParser.apply(root, updated).parseExtension())
      case Raml10Library   => Some(RamlModuleParser(root)(clean).parseModule())
      case f: RamlFragment => RamlFragmentParser(root, f)(updated).parseFragment()
      case _               => None
    }
  }

  private def validateReferences(references: Seq[ParsedReference], ctx: ParserContext): Unit = references.foreach {
    ref =>
      validateJsonPointersToFragments(ref, ctx)
      validateReferencesToLibraries(ref, ctx)
  }

  private def validateJsonPointersToFragments(reference: ParsedReference, ctx: ParserContext): Unit = {
    reference.unit.sourceVendor match {
      case Some(v) if v.isRaml =>
        reference.origin.refs.filter(_.uriFragment.isDefined).foreach { r =>
          ctx.eh.violation(InvalidFragmentRef, "", "Cannot use reference with # in a RAML fragment", r.node)
        }
      case _ => // Nothing to do
    }
  }

  private def validateReferencesToLibraries(reference: ParsedReference, ctx: ParserContext): Unit = {
    val refs: Seq[RefContainer] = reference.origin.refs
    val allKinds                = refs.map(_.linkType)
    val definedKind             = if (allKinds.size > 1) UnspecifiedReference else allKinds.head
    val nodes                   = refs.map(_.node)
    reference.unit match {
      case _: Module => // if is a library, kind should be LibraryReference
        if (allKinds.contains(LibraryReference) && allKinds.contains(LinkReference))
          nodes.foreach(
            ctx.eh
              .violation(ExpectedModule,
                         reference.unit.id,
                         "The !include tag must be avoided when referencing a library",
                         _))
        else if (!LibraryReference.eq(definedKind))
          nodes.foreach(
            ctx.eh.violation(ExpectedModule, reference.unit.id, "Libraries must be applied by using 'uses'", _))
      case _ =>
        // if is not a library, kind should not be LibraryReference
        if (LibraryReference.eq(definedKind))
          nodes.foreach(
            ctx.eh.violation(InvalidInclude, reference.unit.id, "Fragments must be imported by using '!include'", _))
    }
  }

  private def inlineExternalReferences(root: Root, ctx: ParserContext): Unit = {
    root.references.foreach { ref =>
      ref.unit match {
        case e: ExternalFragment =>
          inlineFragment(ref.origin.refs, ref.ast, e.encodes, ref.unit.references, ctx)
        case _ =>
      }
    }
  }

  private def inlineFragment(origins: Seq[RefContainer],
                             document: Option[YNode],
                             encodes: ExternalDomainElement,
                             elementRef: Seq[BaseUnit],
                             ctx: ParserContext): Unit = {
    origins.foreach { refContainer =>
      refContainer.node match {
        case mut: MutRef =>
          elementRef.foreach(u => ctx.addSonRef(u))
          document match {
            case None => mut.target = Some(YNode(encodes.raw.value()))
            case _    => mut.target = document
          }
        case _ =>
      }
    }
  }

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes: Seq[String] = Seq(
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml",
    "text/vnd.yaml"
  )
}

object Raml08Plugin extends RamlPlugin {

  override protected def vendor: Vendor = amf.core.remote.Raml08

  override val validationProfile: ProfileName = Raml08Profile

  def canParse(root: Root): Boolean = {
    RamlHeader(root) exists {
      case Raml08          => true
      case _: RamlFragment => true
      case _               => false
    }
  }

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

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderOptions: RenderOptions,
      shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Option[YDocument] =
    unit match {
      case document: Document => Some(RamlDocumentEmitter(document)(specContext(renderOptions)).emitDocument())
      case fragment: Fragment => Some(new RamlFragmentEmitter(fragment)(specContext(renderOptions)).emitFragment())
      case _                  => None
    }

  override def context(wrapped: ParserContext,
                       root: Root,
                       options: ParsingOptions,
                       ds: Option[WebApiDeclarations] = None): RamlWebApiContext =
    new Raml08WebApiContext(root.location,
                            root.references ++ wrapped.refs,
                            wrapped,
                            ds.map(d => RamlWebApiDeclarations(d)),
                            options = options)

  def specContext(options: RenderOptions): RamlSpecEmitterContext = new Raml08SpecEmitterContext(options.errorHandler)

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit, errorHandler: ErrorHandler, pipelineId: String = "default"): BaseUnit = {
    pipelineId match {
      case ResolutionPipeline.DEFAULT_PIPELINE => new Raml08ResolutionPipeline(errorHandler).resolve(unit)
      case ResolutionPipeline.EDITING_PIPELINE => new Raml08EditingPipeline(errorHandler).resolve(unit)
      case ResolutionPipeline.CACHE_PIPELINE   => new Raml08EditingPipeline(errorHandler, false).resolve(unit)
      case _                                   => super.resolve(unit, errorHandler, pipelineId)
    }
  }

  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    defaultValidationProfiles.filterKeys(_ == validationProfile.p)
}

object Raml10Plugin extends RamlPlugin {

  override protected def vendor: Vendor = amf.core.remote.Raml10

  override val validationProfile: ProfileName = RamlProfile

  def canParse(root: Root): Boolean = RamlHeader(root) exists {
    case Raml10 | Raml10Overlay | Raml10Extension | Raml10Library => true
    case Raml10DocumentationItem | Raml10NamedExample | Raml10DataType | Raml10ResourceType | Raml10Trait |
        Raml10AnnotationTypeDeclaration | Raml10SecurityScheme =>
      true
    case _ => false
  }

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

  override protected def unparseAsYDocument(
      unit: BaseUnit,
      renderOptions: RenderOptions,
      shapeRenderOptions: ShapeRenderOptions = ShapeRenderOptions()): Option[YDocument] =
    unit match {
      case module: Module             => Some(RamlModuleEmitter(module)(specContext(renderOptions)).emitModule())
      case document: Document         => Some(RamlDocumentEmitter(document)(specContext(renderOptions)).emitDocument())
      case external: ExternalFragment => Some(YDocument(YNode(external.encodes.raw.value())))
      case fragment: Fragment         => Some(new RamlFragmentEmitter(fragment)(specContext(renderOptions)).emitFragment())
      case _                          => None
    }

  override def context(wrapped: ParserContext,
                       root: Root,
                       options: ParsingOptions,
                       ds: Option[WebApiDeclarations] = None): RamlWebApiContext =
    new Raml10WebApiContext(root.location,
                            root.references ++ wrapped.refs,
                            wrapped,
                            ds.map(d => RamlWebApiDeclarations(d)),
                            options = options)

  def specContext(options: RenderOptions): RamlSpecEmitterContext = new Raml10SpecEmitterContext(options.errorHandler)

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit,
                       errorHandler: ErrorHandler,
                       pipelineId: String = ResolutionPipeline.DEFAULT_PIPELINE): BaseUnit = pipelineId match {
    case ResolutionPipeline.DEFAULT_PIPELINE => new Raml10ResolutionPipeline(errorHandler).resolve(unit)
    case ResolutionPipeline.EDITING_PIPELINE => new Raml10EditingPipeline(errorHandler).resolve(unit)
    case ResolutionPipeline.COMPATIBILITY_PIPELINE =>
      new CompatibilityPipeline(errorHandler, RamlProfile).resolve(unit)
    case ResolutionPipeline.CACHE_PIPELINE => new Raml10EditingPipeline(errorHandler, false).resolve(unit)
    case _                                 => super.resolve(unit, errorHandler, pipelineId)
  }

  override def domainValidationProfiles(platform: Platform): Map[String, () => ValidationProfile] =
    super
      .domainValidationProfiles(platform)
      .filterKeys(k => k == Raml10Profile.p || k == RamlProfile.p || k == AmfProfile.p)
}
