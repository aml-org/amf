package amf.plugins.domain.webapi

import amf.client.GenerationOptions
import amf.core.Root
import amf.document.Fragment.Fragment
import amf.document._
import amf.domain.{DomainElement, WebApi}
import amf.framework.parser.{Library, Link}
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.webapi.contexts.{OasSpecAwareContext, WebApiContext}
import amf.plugins.domain.webapi.parser.OasHeader
import amf.plugins.domain.webapi.parser.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay}
import amf.remote.{Oas, Platform}
import amf.spec.ParserContext
import amf.spec.oas._

object OAS20Plugin extends AMFDomainPlugin {

  val ID: String = "OAS 2.0"

  val vendors = Seq("OAS 2.0", "OAS")

  private def detectOasUnit(root: Root)(implicit ctx: WebApiContext): Option[BaseUnit] = {
    OasHeader(root) match {
      case Some(Oas20Overlay)   => Some(OasDocumentParser(root).parseOverlay())
      case Some(Oas20Extension) => Some(OasDocumentParser(root).parseExtension())
      case Some(Oas20Header)    => Some(OasDocumentParser(root).parseDocument())
      case f if f.isDefined     => Some(OasFragmentParser(root, f).parseFragment())
      case _                    => None
    }
  }

  /**
    * Decides if this plugin can parse the provided document instance.
    * this can be used by multiple plugins supporting the same media-type
    * to decide which one will parse the document base on information fromt
    * the document structure
    */
  override def canParse(root: Root): Boolean = OasHeader(root) match {
    case Some(Oas20Overlay)   => true
    case Some(Oas20Extension) => true
    case Some(Oas20Header)    => true
    case f if f.isDefined     => true
    case _                    => false
  }

  override def parse(document: Root, parentContext: ParserContext, platform: Platform) = {
    implicit val ctx: WebApiContext = new WebApiContext(Oas, parentContext, OasSpecAwareContext, OasSyntax)
    document.referenceKind match {
      case Library => Some(OasModuleParser(document).parseModule())
      case Link    => Some(OasFragmentParser(document).parseFragment())
      case _       => detectOasUnit(document)
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
    case module: Module     => Some(OasModuleEmitter(module).emitModule())
    case document: Document => Some(OasDocumentEmitter(document).emitDocument())
    case fragment: Fragment => Some(new OasFragmentEmitter(fragment).emitFragment())
    case _                  => None
  }

  override def referenceCollector() = new WebApiReferenceCollector(ID)

  /**
    * List of media types used to encode serialisations of
    * this domain
    */
  override def domainSyntaxes = Seq(
    "application/json",
    "application/yaml",
    "application/x-yaml",
    "text/yaml",
    "text/x-yaml",
    "application/openapi+json",
    "application/swagger+json",
    "application/openapi+yaml",
    "application/swagger+yaml",
    "application/openapi",
    "application/swagger"
  )

}
