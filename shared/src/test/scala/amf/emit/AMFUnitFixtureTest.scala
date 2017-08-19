package amf.emit

import amf.common.AMFAST
import amf.document.Document
import amf.domain._
import amf.remote.Vendor
import amf.unsafe.PlatformSecrets

/**
  *
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def `document/api/bare`: Document = doc(bare())

  def `document/api/basic`: Document = doc(basic())

  def `document/api/advanced`: Document = doc(advanced())

  def `document/api/full`: Document = doc(advanced())

  def ast(document: Document, vendor: Vendor): AMFAST = AMFUnitMaker(document, vendor)

  private def bare(): WebApi = {
    WebApi()
      .withName("test")
      .withDescription("test description")
      .withHost("localhost.com")
      .withSchemes(List("http", "https"))
      .withBasePath("api")
      .withAccepts(List("application/json"))
      .withContentType(List("application/json"))
      .withVersion("1.1")
      .withTermsOfService("termsOfService")
//      .resolveId("file:///tmp/test")
//      .build
  }

  private def basic(): WebApi = {
    val api = bare()
    api
      .withProvider(
        Organization()
          .withEmail("test@test")
          .withName("organizationName")
          .withUrl("organizationUrl")
//          .resolveId(api.getId)
      )
      .withLicense(
        License()
          .withName("licenseName")
          .withUrl("licenseUrl")
//          .resolveId(api.getId)
      )
      .withDocumentation(
        CreativeWork()
          .withUrl("creativoWorkUrl")
          .withDescription("creativeWorkDescription")
//          .resolveId(api.getId)
      )
  }

  private def advanced(): WebApi = {
    val api = basic()

    val endpoint = EndPoint()
      .withDescription("test endpoint")
      .withName("endpoint")
      .withPath("/endpoint")
//      .resolveId(api.getId)

    val get = Operation()
      .withDescription("test operation get")
      .withMethod("get")
      .withName("test get")
      .withSchemes(List("http"))
      .withSummary("summary of operation get")
      .withDocumentation(
        CreativeWork()
          .withDescription("documentation operation")
          .withUrl("localhost:8080/endpoint/operation")
//          .resolveId(get.getId)
      )
//      .resolveId(endpoint.getId)

    val post = Operation()
      .withMethod("post")
      .withDescription("test operation post")
      .withDeprecated(true)
      .withName("test post")
      .withSchemes(List("http"))
      .withSummary("summary of operation post")
      .withDocumentation(
        CreativeWork()
          .withDescription("documentation operation")
          .withUrl("localhost:8080/endpoint/operation")
//        .resolveId(post.getId)
      )
//      .resolveId(endpoint.getId)

    api.withEndPoints(List(endpoint.withOperations(List(get, post))))
  }

  private def doc(api: WebApi): Document =
    Document().withLocation("file:///tmp/test") /*.resolveId("file:///tmp/test")*/.withEncodes(api)
}
