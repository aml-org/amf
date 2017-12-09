package amf.core.client

import amf.core.unsafe.PlatformSecrets
import amf.model.document.BaseUnit

import scala.scalajs.js.annotation.JSExportAll

@JSExportAll
class Resolver(vendor: String) extends PlatformResolver(vendor) with PlatformSecrets {
  def resolve(unit: BaseUnit) = {
    val resolved = super.resolve(unit.element)
    println(s"RESOLVED ${resolved}")
    platform.wrap[BaseUnit](resolved)
  }
}
