package amf.plugins.domain.webapi

import amf.compiler.Root
import amf.document.BaseUnit
import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.webapi.contexts.{RamlSpecAwareContext, WebApiContext}
import amf.plugins.domain.webapi.parser.{RamlFragment, RamlHeader}
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

  def accept(root: Root): Boolean = RamlHeader(root) match {
    case Some(RamlHeader.Raml10)          => true
    case Some(RamlHeader.Raml10Overlay)   => true
    case Some(RamlHeader.Raml10Extension) => true
    case Some(RamlHeader.Raml10Library)   => true
    case Some(fragment: RamlFragment)     => true
    case _                                => false
  }

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
