package amf.emit

import amf.builder.{APIDocumentationBuilder, CreativeWorkBuilder, LicenseBuilder, OrganizationBuilder}
import amf.domain.APIDocumentation
import amf.parser.AMFUnit
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

/**
  *
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def buildCompleteUnit(vendor: Vendor): AMFUnit = {
    val builder = api().toBuilder
    val updated = builder
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
    AMFUnitMaker(updated, vendor)
  }

  def buildSimpleUnit(vendor: Vendor): AMFUnit = {
    AMFUnitMaker(api(), vendor)
  }

  def api(): APIDocumentation = {
    APIDocumentationBuilder()
      .withName("test")
      .withDescription("test description")
      .withHost("http://localhost.com/api")
      .withSchemes(List("http", "https"))
      .withBasePath("http://localhost.com/api")
      .withAccepts("application/json")
      .withContentType("application/json")
      .withVersion("1.1")
      .withTermsOfService("termsOfService")
      .build
  }
}
