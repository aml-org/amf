package amf.core.client

import amf.core.unsafe.PlatformSecrets
import amf.model.document.BaseUnit

class Resolver(vendor: String) extends PlatformResolver(vendor) with PlatformSecrets {
  def resolve(unit: BaseUnit): BaseUnit = platform.wrap(super.resolve(unit.element))
}
