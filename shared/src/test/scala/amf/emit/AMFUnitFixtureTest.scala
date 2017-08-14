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

  def `document/api/bare`: Document = doc(bare)

  def `document/api/basic`: Document = doc(basic)

  def `document/api/advanced`: Document = doc(advanced)

  def `document/api/full`: Document = doc(advanced)

  def ast(document: Document, vendor: Vendor): AMFAST = AMFUnitMaker(document, vendor)

  private def bare(): WebApi = {
    WebApiBuilder()
      .withName("test")
      .withDescription("test description")
      .withHost("http://localhost.com/api")
      .withSchemes(List("http", "https"))
      .withBasePath("http://localhost.com/api")
      .withAccepts(List("application/json"))
      .withContentType(List("application/json"))
      .withVersion("1.1")
      .withTermsOfService("termsOfService")
      .resolveId("file:///tmp/test")
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
          .resolveId(builder.getId)
          .build
      )
      .withLicense(
        LicenseBuilder()
          .withName("licenseName")
          .withUrl("licenseUrl")
          .resolveId(builder.getId)
          .build
      )
      .withDocumentation(
        CreativeWorkBuilder()
          .withUrl("creativoWorkUrl")
          .withDescription("creativeWorkDescription")
          .resolveId(builder.getId)
          .build
      )
      .build
  }

  private def advanced(): WebApi = {
    val builder = basic().toBuilder

    val endpoint = new EndPointBuilder()
      .withDescription("test endpoint")
      .withName("endpoint")
      .withPath("/endpoint")
      .resolveId(builder.getId)

    val get = new OperationBuilder()
      .withDescription("test operation get")
      .withMethod("get")
      .withName("test get")
      .withSchemes(List("http"))
      .withSummary("summary of operation get")
      .resolveId(endpoint.getId)

    val post = new OperationBuilder()
      .withMethod("post")
      .withDescription("test operation post")
      .withDeprecated(true)
      .withName("test post")
      .withSchemes(List("http"))
      .withSummary("summary of operation post")
      .resolveId(endpoint.getId)

    val getBuilt = get
      .withDocumentation(
        new CreativeWorkBuilder()
          .withDescription("documentation operation")
          .withUrl("localhost:8080/endpoint/operation")
          .resolveId(get.getId)
          .build)
      .build

    val postBuilt = post
      .withDocumentation(
        new CreativeWorkBuilder()
          .withDescription("documentation operation")
          .withUrl("localhost:8080/endpoint/operation")
          .resolveId(post.getId)
          .build)
      .build

    builder
      .withEndPoints(List(endpoint.withOperations(List(getBuilt, postBuilt)).build))
      .build
  }

  private def doc(api: () => WebApi): Document =
    DocumentBuilder().withLocation("file:///tmp/test").resolveId("file:///tmp/test").withEncodes(api()).build
}
