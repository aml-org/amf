package amf.plugins.domain.webapi

import amf.compiler.{RamlFragment, RamlHeader, Root}
import amf.document.BaseUnit
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.webapi.contexts.{RamlSpecAwareContext, WebApiContext}
import amf.remote.Raml
import amf.spec.ParserContext
import amf.spec.raml.{RamlDocumentParser, RamlFragmentParser, RamlModuleParser, RamlSyntax}

class RAML10Plugin extends AMFDomainPlugin {
  override def parse(root: Root, parentContext: ParserContext): Option[BaseUnit] = {
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
}
