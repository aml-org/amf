package amf.plugins.document.webapi

import amf.ProfileNames.RAML
import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.model.document._
import amf.core.model.domain.{DomainElement, ExternalDomainElement}
import amf.core.parser.{EmptyFutureDeclarations, ParserContext}
import amf.core.remote.Platform
import amf.plugins.document.webapi.contexts._
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.RamlHeader._
import amf.plugins.document.webapi.parser.spec.WebApiDeclarations
import amf.plugins.document.webapi.parser.spec.raml.{RamlFragmentEmitter, RamlModuleEmitter, _}
import amf.plugins.document.webapi.parser.{RamlFragment, RamlHeader}
import amf.plugins.document.webapi.resolution.pipelines.RamlResolutionPipeline
import amf.plugins.domain.webapi.models.WebApi
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YDocument, YNode}
import org.yaml.parser.YamlParser

trait RAMLPlugin extends BaseWebApiPlugin {

  override val ID: String = "RAML " + version

  override val vendors = Seq(ID, "RAML")

  override val validationProfile: String = RAML

  def context(wrapped: ParserContext, ds: Option[WebApiDeclarations] = None): RamlWebApiContext

  override def parse(root: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    inlineExternalReferences(root)

    val updated     = context(parentContext)
    val cleanNested = ParserContext(root.location, root.references, EmptyFutureDeclarations())
    val clean       = context(cleanNested)

    RamlHeader(root) flatMap { // todo review this, should we use the raml web api context for get the version parser?
      case Raml08          => Some(Raml08DocumentParser(root)(updated).parseDocument())
      case Raml10          => Some(Raml10DocumentParser(root)(updated).parseDocument())
      case Raml10Overlay   => Some(Raml10DocumentParser(root)(updated).parseOverlay())
      case Raml10Extension => Some(Raml10DocumentParser(root)(updated).parseExtension())
      case Raml10Library   => Some(RamlModuleParser(root)(clean).parseModule())
      case f: RamlFragment => RamlFragmentParser(root, f)(updated).parseFragment()
      case _               => None
    }
  }

  private def inlineFragment(ast: YNode, encodes: ExternalDomainElement): Unit = {
    ast match {
      case mut: MutRef =>
        if (isRamlOrYaml(encodes)) inlineYNode(mut, encodes.raw)
        else inlineYScalar(mut, encodes.raw)
      case _ =>
    }
  }

  private def inlineYNode(ref: MutRef, raw: String): Unit = {
    YamlParser(raw).parse() match {
      case Seq(document: YDocument) => ref.target = Some(document.node)
    }
  }

  def inlineExternalReferences(root: Root): Unit = {
    root.references.filter(_.isExternalFragment).foreach { ref =>
      ref.unit match {
        case e: ExternalFragment => inlineFragment(ref.origin.ast, e.encodes)
      }
    }
  }

  private def inlineYScalar(ref: MutRef, raw: String): Unit = ref.target = Some(YNode(raw))

  private def isRamlOrYaml(encodes: ExternalDomainElement) = documentSyntaxes.contains(encodes.mediaType)

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def documentSyntaxes = Seq(
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml",
    "text/vnd.yaml"
  )

  /**
    * Resolves the provided base unit model, according to the semantics of the domain of the document
    */
  override def resolve(unit: BaseUnit): BaseUnit = new RamlResolutionPipeline().resolve(unit)
}

object RAML08Plugin extends RAMLPlugin {
  override def version: String = "0.8"

  def canParse(root: Root): Boolean = RamlHeader(root) exists {
    case Raml08          => true
    case _: RamlFragment => true
    case _               => false
  }

  // fix for 08
  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Overlay                           => false
    case _: Extension                         => false
    case document: Document                   => document.encodes.isInstanceOf[WebApi]
    case module: Module                       => false
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

  // fix for 08?
  override def unparse(unit: BaseUnit, options: GenerationOptions): Option[YDocument] = unit match {
    case document: Document => Some(Raml08DocumentEmitter(document).emitDocument())
    case fragment: Fragment => Some(new RamlFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

  override def context(wrapped: ParserContext, ds: Option[WebApiDeclarations] = None): RamlWebApiContext =
    new Raml08WebApiContext(wrapped, ds)
}

object RAML10Plugin extends RAMLPlugin {

  override def version: String = "1.0"

  def canParse(root: Root): Boolean = RamlHeader(root) exists {
    case Raml10 | Raml10Overlay | Raml10Extension | Raml10Library => true
    case _: RamlFragment                                          => true
    case _                                                        => false
  }

  override def canUnparse(unit: BaseUnit): Boolean = unit match {
    case _: Overlay         => true
    case _: Extension       => true
    case document: Document => document.encodes.isInstanceOf[WebApi]
    case module: Module =>
      module.declares exists {
        case _: DomainElement => true
        case _                => false
      }
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

  // fix for 08?
  override def unparse(unit: BaseUnit, options: GenerationOptions): Option[YDocument] = unit match {
    case module: Module     => Some(RamlModuleEmitter(module).emitModule())
    case document: Document => Some(Raml10DocumentEmitter(document).emitDocument())
    case fragment: Fragment => Some(new RamlFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

  override def context(wrapped: ParserContext, ds: Option[WebApiDeclarations] = None): RamlWebApiContext =
    new Raml10WebApiContext(wrapped, ds)
}
