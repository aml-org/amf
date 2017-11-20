package amf.plugins.domain.webapi

import amf.framework.plugins.AMFDomainPlugin
import amf.plugins.domain.webapi.contexts.{RamlSpecAwareContext, WebApiContext}
import amf.remote.Raml
import amf.spec.ParserContext
import amf.spec.raml.RamlSyntax
import org.yaml.model.YDocument

class Raml10Plugin extends AMFDomainPlugin {
  override def parse(document: YDocument, parentContext: ParserContext) = {
    implicit val ctx = new WebApiContext(Raml, parentContext, RamlSpecAwareContext, RamlSyntax)
    ???
  }
}
