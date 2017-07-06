package amf.unsafe

import amf.builder._

/**
  * Created by martin.gutierrez on 7/6/17.
  */
object BuilderFactory {
  def webApiBuilder: BaseWebApiBuilder         = new WebApiBuilder
  def licenseBuilder: LicenseBuilder           = LicenseBuilder()
  def creativeWorkBuilder: CreativeWorkBuilder = CreativeWorkBuilder()
  def organizationBuilder: OrganizationBuilder = OrganizationBuilder()
}
