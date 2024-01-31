package amf.apicontract.internal.spec.common.emitter

import amf.apicontract.client.scala.model.domain.security.SecurityScheme
import amf.apicontract.internal.spec.common.parser.SecuritySchemeParser
import amf.shapes.internal.spec.common.parser.YMapEntryLike
import org.yaml.model.YPart

trait SpecAwareContext {
  val factory: SpecVersionFactory
}

trait SpecVersionFactory {
  def securitySchemeParser: (YMapEntryLike, SecurityScheme => SecurityScheme) => SecuritySchemeParser
}
