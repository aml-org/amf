package amf.broker

import amf.builder.{CreativeWorkBuilder, LicenseBuilder, OrganizationBuilder}
import amf.model.BaseWebApi
import amf.parser.AMFUnit
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

/**
  * Created by hernan.najles on 7/11/17.
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def buildCompleteUnit(vendor: Vendor): AMFUnit = {
    val webApi  = buildWebApiClass()
    val builder = webApi.toBuilder
    val newWebApi = builder
      .withProvider(
        OrganizationBuilder()
          .withEmail("test@test")
          .withName("organizationName")
          .withUrl("organizationUrl")
          .build
      )
      .withLicense(
        LicenseBuilder()
          .withName("licenseName")
          .withUrl("licenseUrl")
          .build
      )
      .withDocumentation(
        CreativeWorkBuilder()
          .withUrl("creativoWorkUrl")
          .withDescription("creativeWorkDescription")
          .build
      )
      .build
    AMFUnitMaker(newWebApi, vendor)
  }

  def buildSimpleUnit(vendor: Vendor): AMFUnit = {
    val webApi = buildWebApiClass()
    AMFUnitMaker(webApi, vendor)
  }

  def buildWebApiClass(): BaseWebApi = {
    builders.webApi
      .withName("test")
      .withDescription("test description")
      .withHost("http://localhost.com/api")
      .withSchemes(List("http", "https"))
      .withBasePath("api")
      .withAccepts("application/json")
      .withContentType("application/json")
      .withVersion("1.1")
      .withTermsOfService("termsOfService")
      .build
  }
}
