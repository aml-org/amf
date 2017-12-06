package amf.plugins.document.webapi

import amf.ProfileNames.RAML
import amf.core.Root
import amf.core.client.GenerationOptions
import amf.core.model.document._
import amf.core.model.domain.DomainElement
import amf.core.parser.{EmptyFutureDeclarations, ParserContext}
import amf.core.remote.Platform
import amf.plugins.document.webapi.contexts.{RamlSpecAwareContext, WebApiContext}
import amf.plugins.document.webapi.model._
import amf.plugins.document.webapi.parser.RamlHeader.{Raml10, Raml10Extension, Raml10Library, Raml10Overlay}
import amf.plugins.document.webapi.parser.spec.raml.{RamlDocumentEmitter, RamlFragmentEmitter, RamlModuleEmitter, _}
import amf.plugins.document.webapi.parser.{RamlFragment, RamlHeader}
import amf.plugins.document.webapi.resolution.pipelines.RamlResolutionPipeline
import amf.plugins.domain.webapi.models.WebApi
import org.yaml.model.YNode.MutRef
import org.yaml.model.{YDocument, YNode}

object RAML10Plugin extends BaseWebApiPlugin {

  override val ID: String = "RAML 1.0"

  override val vendors = Seq("RAML 1.0", "RAML")

  override val validationProfile: String = RAML

  def canParse(root: Root): Boolean = RamlHeader(root) exists {
    case Raml10 | Raml10Overlay | Raml10Extension | Raml10Library => true
    case _: RamlFragment                                          => true
    case _                                                        => false
  }

  override def parse(root: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    inlineExternalReferences(root)

    val updated     = new WebApiContext(RamlSyntax, RAML, RamlSpecAwareContext, parentContext)
    val cleanNested = ParserContext(root.location, root.references, EmptyFutureDeclarations())
    val clean       = new WebApiContext(RamlSyntax, RAML, RamlSpecAwareContext, cleanNested)

    RamlHeader(root) flatMap {
      case Raml10          => Some(RamlDocumentParser(root)(updated).parseDocument())
      case Raml10Overlay   => Some(RamlDocumentParser(root)(updated).parseOverlay())
      case Raml10Extension => Some(RamlDocumentParser(root)(updated).parseExtension())
      case Raml10Library   => Some(RamlModuleParser(root)(clean).parseModule())
      case f: RamlFragment => RamlFragmentParser(root, f)(updated).parseFragment()
      case _               => None
    }
  }

  def inlineExternalReferences(root: Root): Unit = {
    root.references.filter(_.isExternalFragment).foreach { ref =>
      ref.unit match {
        case external: ExternalFragment =>
          ref.origin.ast match {
            case mut: MutRef => mut.target = Some(YNode(external.encodes.raw))
            case _           =>
          }
      }
    }
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

  override def unparse(unit: BaseUnit, options: GenerationOptions): Option[YDocument] = unit match {
    case module: Module     => Some(RamlModuleEmitter(module).emitModule())
    case document: Document => Some(RamlDocumentEmitter(document).emitDocument())
    case fragment: Fragment => Some(new RamlFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

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
