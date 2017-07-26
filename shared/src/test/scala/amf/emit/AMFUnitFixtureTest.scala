package amf.emit

import amf.builder._
import amf.common.AMFAST
import amf.document.Document
import amf.domain.WebApi
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

/**
  *
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def `document/api/bare`: Document     = doc(bare)
  def `document/api/basic`: Document    = doc(basic)
  def `document/api/advanced`: Document = doc(advanced)
  def `document/api/full`: Document     = doc(advanced)

  def ast(document: Document, vendor: Vendor): AMFAST = AMFUnitMaker(document, vendor)

  private def bare(): WebApi = {
    WebApiBuilder()
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

  private def basic(): WebApi = {
    val builder = bare().toBuilder
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

  private def advanced(): WebApi = {
    val builder = basic().toBuilder
    val documentation = new CreativeWorkBuilder()
      .withDescription("documentation operation")
      .withUrl("localhost:8080/endpoint/operation")
      .build

    val get = new OperationBuilder()
      .withDescription("test operation get")
      .withDocumentation(documentation)
      .withMethod("get")
      .withName("test get")
      .withSchemes(List("http"))
      .withSummary("summary of operation get")
      .build

    val post = new OperationBuilder()
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
      .withOperations(List(get, post))
      .build

    builder
      .withEndPoints(List(endpoint))
      .build
  }

  private def doc(api: () => WebApi): Document = DocumentBuilder().withEncodes(api()).build
}
