package amf.emit

import amf.core.client.scala.model.document.{Document, Module}
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.unsafe.PlatformSecrets
import amf.plugins.domain.apicontract.models.api.WebApi
import amf.plugins.domain.apicontract.models.{License, Organization}
import amf.plugins.domain.shapes.models.{CreativeWork, ScalarShape}

/**
  *
  */
trait AMFUnitFixtureTest extends PlatformSecrets {

  def `document/api/bare`: Document = doc(bare())

  def `document/api/basic`: Document = doc(basic())

  def `document/api/advanced`: Document = doc(advanced())

  def `document/api/full`: Document = doc(advanced())

  def `module/bare`: Module = libraryBare()

  def `document/api/stringExamples`: Document = doc(stringExamples())

  private def bare(): WebApi = {
    val api: WebApi = WebApi()
    api
      .withName("test")
      .withDescription("test description")
      .withSchemes(List("http", "https"))
      .withAccepts(List("application/json"))
      .withContentType(List("application/json"))
      .withVersion("1.1")
      .withTermsOfService("termsOfService")
      .adopted("file:///tmp/test")
      .withServer("localhost.com/api")
      .add(SynthesizedField())
    api
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

  private def stringExamples(): WebApi = {
    val webApi   = WebApi().withName("test examples")
    val endpoint = webApi.withEndPoint("/endpoint")
    val response = endpoint.withOperation("get").withResponse("200")
    response.withExample("application/json").withValue("name: Cristian\nlastName: Pavon\n")
    val payload       = response.withPayload(Some("application/json"))
    val shape         = payload.withObjectSchema("person")
    val nameShape     = ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")
    val lastNameShape = ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string")
    shape.withProperty("name").withMinCount(1).withRange(nameShape)
    shape.withProperty("lastName").withMinCount(1).withRange(lastNameShape)
    shape.withExample(None).withValue("name: roman\nlastName: riquelme\n")

    webApi
  }

  def libraryBare(): Module = {
    Module()
      .withId("file://amf-cli/shared/src/test/resources/clients/lib/lib.raml")
      .withLocation("file://amf-cli/shared/src/test/resources/clients/lib/lib.raml")
      .withUsage("Data types and annotation types")

  }

  private def doc(api: WebApi): Document =
    Document().withLocation("file:///tmp/test").adopted("file:///tmp/test").withEncodes(api).withRoot(true)
}
