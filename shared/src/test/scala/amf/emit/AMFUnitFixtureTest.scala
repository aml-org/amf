package amf.emit

import amf.framework.model.document.{Document, Module}
import amf.domain._
import amf.plugins.domain.webapi.models.{CreativeWork, WebApi}
import amf.unsafe.PlatformSecrets

/**
  *
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def `document/api/bare`: Document = doc(bare())

  def `document/api/basic`: Document = doc(basic())

  def `document/api/advanced`: Document = doc(advanced())

  def `document/api/full`: Document = doc(advanced())

  def `module/bare`: Module = libraryBare()

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
      .adopted("file:///tmp/test")
  }

  private def basic(): WebApi = {
    val api = bare()
    api
      .withProvider(
        Organization()
          .withEmail("test@test")
          .withName("organizationName")
          .withUrl("organizationUrl")
      )
      .withLicense(
        License()
          .withName("licenseName")
          .withUrl("licenseUrl")
      )
      .withDocumentationUrl("creativoWorkUrl")
      .withDescription("creativeWorkDescription")

    api
  }

  private def advanced(): WebApi = {
    val api = basic()

    val endpoint = api
      .withEndPoint("/endpoint")
      .withDescription("test endpoint")
      .withName("endpoint")

    endpoint
      .withOperation("get")
      .withDescription("test operation get")
      .withName("test get")
      .withSchemes(List("http"))
      .withSummary("summary of operation get")
      .withDocumentation(
        CreativeWork()
          .withDescription("documentation operation")
          .withUrl("localhost:8080/endpoint/operation")
      )

    endpoint
      .withOperation("post")
      .withDescription("test operation post")
      .withDeprecated(true)
      .withName("test post")
      .withSchemes(List("http"))
      .withSummary("summary of operation post")
      .withDocumentation(
        CreativeWork()
          .withDescription("documentation operation")
          .withUrl("localhost:8080/endpoint/operation")
      )

    api
  }

  def libraryBare(): Module = {
    Module()
      .withId("file://shared/src/test/resources/clients/lib/lib.raml")
      .withLocation("file://shared/src/test/resources/clients/lib/lib.raml")
      .withUsage("Data types and annotation types")

  }

  private def doc(api: WebApi): Document =
    Document().withLocation("file:///tmp/test").adopted("file:///tmp/test").withEncodes(api)
}
