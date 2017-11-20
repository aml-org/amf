package amf.plugins.domain.webapi

import amf.compiler.Root
import amf.document.BaseUnit
import amf.framework.parser.{Library, Link}
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.webapi.contexts.{OasSpecAwareContext, WebApiContext}
import amf.plugins.domain.webapi.parser.OasHeader
import amf.plugins.domain.webapi.parser.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay}
import amf.remote.Oas
import amf.spec.ParserContext
import amf.spec.oas.{OasDocumentParser, OasFragmentParser, OasModuleParser, OasSyntax}

class OAS20Plugin extends AMFDomainPlugin {
  override def parse(document: Root, parentContext: ParserContext) = {
    implicit val ctx: WebApiContext = new WebApiContext(Oas, parentContext, OasSpecAwareContext, OasSyntax)
    document.referenceKind match {
      case Library => Some(OasModuleParser(document).parseModule())
      case Link    => Some(OasFragmentParser(document).parseFragment())
      case _       => detectOasUnit(document)
    }
  }

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
  override def accept(root: Root): Boolean = OasHeader(root) match {
    case Some(Oas20Overlay)   => true
    case Some(Oas20Extension) => true
    case Some(Oas20Header)    => true
    case f if f.isDefined     => true
    case _                    => false
  }

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
