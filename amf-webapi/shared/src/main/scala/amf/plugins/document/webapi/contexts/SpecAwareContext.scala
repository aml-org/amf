package amf.plugins.document.webapi.contexts
import amf.plugins.document.webapi.parser.spec.declaration.SecuritySchemeParser
import amf.plugins.domain.webapi.models.security.SecurityScheme
import org.yaml.model.YPart

trait SpecAwareContext {
  val factory: SpecVersionFactory
}
trait SpecVersionFactory {
  def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser
}
