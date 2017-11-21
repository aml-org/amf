package amf.plugins.domain.webapi

import amf.client.GenerationOptions
import amf.core.Root
import amf.document.Fragment.Fragment
import amf.document._
import amf.domain.{DomainElement, WebApi}
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.webapi.contexts.{RamlSpecAwareContext, WebApiContext}
import amf.plugins.domain.webapi.parser.{RamlFragment, RamlHeader}
import amf.remote.{Platform, Raml}
import amf.spec.ParserContext
import amf.spec.raml._

object RAML10Plugin extends AMFDomainPlugin {

  val ID: String = "RAML 1.0"

  val vendors = Seq("RAML 1.0", "RAML")

  def canParse(root: Root): Boolean = RamlHeader(root) match {
    case Some(RamlHeader.Raml10)          => true
    case Some(RamlHeader.Raml10Overlay)   => true
    case Some(RamlHeader.Raml10Extension) => true
    case Some(RamlHeader.Raml10Library)   => true
    case Some(fragment: RamlFragment)     => true
    case _                                => false
  }

  override def parse(root: Root, parentContext: ParserContext, platform: Platform): Option[BaseUnit] = {
    val updated: WebApiContext = new WebApiContext(Raml, parentContext, RamlSpecAwareContext, RamlSyntax)
    val clean: ParserContext = ParserContext(parentContext.validation, root.location, root.references)
    RamlHeader(root) match {
      case Some(RamlHeader.Raml10)          => Some(RamlDocumentParser(root)(updated).parseDocument())
      case Some(RamlHeader.Raml10Overlay)   => Some(RamlDocumentParser(root)(updated).parseOverlay())
      case Some(RamlHeader.Raml10Extension) => Some(RamlDocumentParser(root)(updated).parseExtension())
      case Some(RamlHeader.Raml10Library)   => Some(RamlModuleParser(root)(clean.toRaml).parseModule())
      case Some(fragment: RamlFragment)     => RamlFragmentParser(root, fragment)(updated).parseFragment()
      case _                                => None
    }
  }

  override def canUnparse(unit: BaseUnit) = unit match {
    case _: Overlay   => true
    case _: Extension => true
    case document: Document => document.encodes.isInstanceOf[WebApi]
    case module: Module =>
      module.declares exists {
        case _:DomainElement  => true
        case _                => false
      }
    case _: Fragment  => true
    case _            => false
  }

  override def unparse(unit: BaseUnit, options: GenerationOptions) = unit match {
    case module: Module     => Some(RamlModuleEmitter(module).emitModule())
    case document: Document => Some(RamlDocumentEmitter(document).emitDocument())
    case fragment: Fragment => Some(new RamlFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

  override def referenceCollector() = new WebApiReferenceCollector(ID)

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def domainSyntaxes = Seq(
    "application/raml",
    "application/raml+json",
    "application/raml+yaml",
    "text/yaml",
    "text/x-yaml",
    "application/yaml",
    "application/x-yaml"
  )

}
