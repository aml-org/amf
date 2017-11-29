package amf.core.client

import amf.core.unsafe.PlatformSecrets
import amf.model.document.BaseUnit

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class Resolver(vendor: String) extends PlatformResolver(vendor) with PlatformSecrets {
  def resolve(unit: BaseUnit) = platform.wrap(super.resolve(unit.element))
}
