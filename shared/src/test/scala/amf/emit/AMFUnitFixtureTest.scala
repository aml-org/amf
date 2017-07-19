package amf.emit

import amf.builder._
import amf.domain.APIDocumentation
import amf.parser.AMFUnit
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

/**
  *
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def buildCompleteUnit(vendor: Vendor): AMFUnit = {

    AMFUnitMaker(apiComplete(), vendor)
  }

  def buildExtendedUnit(vendor: Vendor): AMFUnit = {
    val builder = apiComplete().toBuilder
    val documentation = new CreativeWorkBuilder()
      .withDescription("documentation operation")
      .withUrl("localhost:8080/endpoint/operation")
      .build

    val operationGet = new OperationBuilder()
      .withDescription("test operation get")
      .withDocumentation(documentation)
      .withMethod("get")
      .withName("test get")
      .withSchemes(List("http"))
      .withSummary("summary of operation get")
      .build

    val operationPost = new OperationBuilder()
      .withDescription("test operation post")
      .withDocumentation(documentation)
      .withMethod("post")
      .withName("test post")
      .withSchemes(List("http"))
      .withSummary("summary of operation post")
      .build

    val endpoint = new EndPointBuilder()
      .withDescription("test endpoint")
      .withName("endpoint")
      .withPath("/endpoint")
      .withOperations(List(operationGet, operationPost))
      .build

    val api = builder
      .withEndPoints(List(endpoint))
      .build
    AMFUnitMaker(api, vendor)
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

  def apiComplete(): APIDocumentation = {
    val builder = api().toBuilder
    builder
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

  }
}
