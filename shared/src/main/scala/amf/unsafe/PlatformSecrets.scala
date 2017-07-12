package amf.unsafe

import amf.remote._
import amf.builder._

trait PlatformSecrets {
  val platform: Platform = PlatformBuilder()

  object builders {

    def webApi: BaseWebApiBuilder         = new WebApiBuilder
    def license: LicenseBuilder           = LicenseBuilder()
    def creativeWork: CreativeWorkBuilder = CreativeWorkBuilder()
    def organization: OrganizationBuilder = OrganizationBuilder()
    def endPoint: EndPointBuilder         = EndPointBuilder()
  }
}
