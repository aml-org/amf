package amf.plugins.document.apicontract.contexts
import amf.plugins.document.apicontract.parser.spec.declaration.SecuritySchemeParser
import amf.plugins.domain.apicontract.models.security.SecurityScheme
import org.yaml.model.YPart

trait SpecAwareContext {
  val factory: SpecVersionFactory
}

trait SpecVersionFactory {
  def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser
}
