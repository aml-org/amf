package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.internal.spec.common.parser.SecuritySchemeParser
import org.yaml.model.YPart

trait SpecAwareContext {
  val factory: SpecVersionFactory
}

trait SpecVersionFactory {
  def securitySchemeParser: (YPart, SecurityScheme => SecurityScheme) => SecuritySchemeParser
}
