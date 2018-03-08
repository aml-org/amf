package amf.core.client

import amf.core.unsafe.PlatformSecrets
import amf.client.model.document.BaseUnit

class Resolver(vendor: String) extends PlatformResolver(vendor) with PlatformSecrets {
  def resolve(unit: BaseUnit): BaseUnit = platform.wrap[BaseUnit](super.resolve(unit._internal))
}
