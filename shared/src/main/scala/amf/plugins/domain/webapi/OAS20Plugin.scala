package amf.plugins.domain.webapi

import amf.compiler.OasHeader.{Oas20Extension, Oas20Header, Oas20Overlay}
import amf.compiler.{OasHeader, Root}
import amf.document.BaseUnit
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.framework.parser.{Library, Link}
import amf.plugins.domain.webapi.contexts.{OasSpecAwareContext, WebApiContext}
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

}
