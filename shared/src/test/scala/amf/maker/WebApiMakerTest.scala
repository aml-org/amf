package amf.maker

import amf.common.{AmfObjectTestMatcher, ListAssertions}
import amf.compiler.AMFCompiler
import amf.document.Document
import amf.domain.{License, _}
import amf.metadata.Field
import amf.model.AmfObject
import amf.remote.{Hint, OasJsonHint, RamlYamlHint}
import amf.shape.ScalarShape
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

class WebApiMakerTest extends AsyncFunSuite with PlatformSecrets with ListAssertions with AmfObjectTestMatcher {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/maker/"

  test("Generate complete web api instance") {

    val api = WebApi()
      .withName("test")
      .withDescription("testDescription")
      .withHost("api.example.com")
      .withSchemes(List("http", "https"))
      .withBasePath("/path")
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withTermsOfService("terminos")
      .withProvider(Organization().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto"))
      .withLicense(License().withUrl("urlLicense").withName("nameLicense"))
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))

    assertFixture(api, "completeExample.raml", RamlYamlHint)
  }

  test("WebApi with nested endpoints - RAML.") {
    val endpoints = List(
      EndPoint()
        .withPath("/levelzero"),
      EndPoint()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!"),
      EndPoint().withPath("/levelzero/another-level-one").withName("some other display name"),
      EndPoint().withPath("/another-levelzero").withName("Root name")
    )

    val api = WebApi()
      .withName("API")
      .withBasePath("/some/base/uri")
      .withEndPoints(endpoints)

    assertFixture(api, "nested-endpoints.raml", RamlYamlHint)
  }

  test("WebApi with nested endpoints - OAS.") {
    val endpoints = List(
      EndPoint()
        .withPath("/levelzero")
        .withName("Name"),
      EndPoint()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!"),
      EndPoint().withPath("/levelzero/another-level-one").withName("some other display name"),
      EndPoint().withPath("/another-levelzero").withName("Root name")
    )
    val api = WebApi()
      .withName("API")
      .withBasePath("/some/base/uri")
      .withEndPoints(endpoints)

    assertFixture(api, "nested-endpoints.json", OasJsonHint)
  }

  test("WebApi with multiple operations - RAML.") {
    val api = WebApi()
      .withName("API")
      .withBasePath("/some/base/uri")
    api.withEndPoint("/levelzero")

    val endpointOne = api.withEndPoint("/levelzero/level-one")

    val operationGet = endpointOne
      .withName("One display name")
      .withDescription("and this description!")
      .withOperation("get")
    val operationPost = endpointOne.withOperation("post")

    operationGet
      .withName("Some title")
      .withDescription("Some description")
      .withDeprecated(true)
      .withSummary("This is a summary")
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))
      .withSchemes(List("http", "https"))
    operationPost
      .withName("Some title")
      .withDescription("Some description")
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))
      .withSchemes(List("http", "https"))

    assertFixture(api, "endpoint-operations.raml", RamlYamlHint)
  }

  test("WebApi with multiple operations - OAS.") {
    val api = WebApi()
      .withName("API")
      .withBasePath("/some/base/uri")

    api.withEndPoint("/levelzero").withName("Name")

    val endpointOne = api
      .withEndPoint("/levelzero/level-one")
      .withName("One display name")
      .withDescription("and this description!")

    val operationGet = endpointOne.withOperation("get")
    operationGet
      .withName("Some title")
      .withDescription("Some description")
      .withDeprecated(true)
      .withSummary("This is a summary")
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))
      .withSchemes(List("http", "https"))
    endpointOne
      .withOperation("post")
      .withName("Some title")
      .withDescription("Some description")
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))
      .withSchemes(List("http", "https"))

    assertFixture(api, "endpoint-operations.json", OasJsonHint)
  }

  test("Parameters - RAML.") {
    val endpoints = List(
      EndPoint()
        .withPath("/levelzero/some{two}")
        .withParameters(List(Parameter().withName("two").withRequired(false).withBinding("path"))),
      EndPoint()
        .withPath("/levelzero/some{two}/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(List(
          Operation()
            .withMethod("get")
            .withName("Some title")
            .withRequest(Request()
              .withQueryParameters(List(
                Parameter().withName("param1").withDescription("Some descr").withRequired(true).withBinding("query"),
                Parameter()
                  .withName("param2")
                  .withSchema(ScalarShape().withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  .withRequired(false)
                  .withBinding("query")
              ))),
          Operation()
            .withMethod("post")
            .withName("Some title")
            .withDescription("Some description")
            .withRequest(Request()
              .withHeaders(List(
                Parameter().withName("Header-One").withRequired(false).withBinding("header")
              )))
        ))
    )
    val api = WebApi()
      .withName("API")
      .withBasePath("/some/{one}/uri")
      .withBaseUriParameters(
        List(Parameter().withName("one").withRequired(true).withDescription("One base uri param").withBinding("path")))
      .withEndPoints(endpoints)

    assertFixture(api, "operation-request.raml", RamlYamlHint)
  }

  test("Parameters - OAS.") {
    val endpoints = List(
      EndPoint()
        .withPath("/levelzero")
        .withName("Name"),
      EndPoint()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(List(
          Operation()
            .withMethod("get")
            .withName("Some title")
            .withRequest(
              Request()
                .withQueryParameters(
                  List(
                    Parameter()
                      .withName("param1")
                      .withDescription("Some descr")
                      .withRequired(true)
                      .withBinding("query")
                  ))
                .withHeaders(List(Parameter()
                  .withName("param2?")
                  .withSchema(ScalarShape().withDataType("string"))
                  .withRequired(false)
                  .withBinding("header")))
                .withPayloads(List(
                  Payload().withSchema(ScalarShape().withDataType("string")).withMediaType("application/xml")))
            ),
          Operation()
            .withMethod("post")
            .withName("Some title")
            .withDescription("Some description")
            .withRequest(
              Request()
                .withHeaders(List(
                  Parameter().withName("Header-One").withRequired(false).withBinding("header")
                ))
                .withPayloads(
                  List(Payload().withSchema(ScalarShape().withDataType("number")).withMediaType("application/json")))
            )
        ))
    )
    val api = WebApi()
      .withName("API")
      .withBasePath("/some/base/uri")
      .withEndPoints(endpoints)

    assertFixture(api, "operation-request.json", OasJsonHint)
  }

  test("Responses - RAML.") {
    val endpoints = List(
      EndPoint()
        .withPath("/levelzero"),
      EndPoint()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(
          List(
            Operation()
              .withMethod("get")
              .withName("Some title")
              .withRequest(Request()
                .withPayloads(List(Payload().withMediaType("application/json"))))
              .withResponses(List(
                Response()
                  .withDescription("200 descr")
                  .withStatusCode("200")
                  .withName("200")
                  .withHeaders(List(
                    Parameter()
                      .withName("Time-Ago")
                      .withSchema(ScalarShape().withDataType("integer"))
                      .withRequired(true)
                  )),
                Response()
                  .withName("404")
                  .withStatusCode("404")
                  .withDescription("Not found!")
                  .withPayloads(List(
                    Payload().withMediaType("application/json").withSchema(ScalarShape().withDataType("string")),
                    Payload().withMediaType("application/xml").withSchema(ScalarShape().withDataType("string"))
                  ))
              ))
          ))
    )

    val api = WebApi()
      .withName("API")
      .withBasePath("/some/uri")
      .withEndPoints(endpoints)

    assertFixture(api, "operation-response.raml", RamlYamlHint)
  }

  test("Responses - OAS.") {
    val endpoints = List(
      EndPoint()
        .withPath("/levelzero")
        .withName("Name"),
      EndPoint()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(
          List(
            Operation()
              .withMethod("get")
              .withName("Some title")
              .withRequest(Request()
                .withPayloads(
                  List(Payload().withMediaType("application/json").withSchema(ScalarShape().withDataType("number")))))
              .withResponses(List(
                Response()
                  .withDescription("200 descr")
                  .withStatusCode("200")
                  .withName("default")
                  .withHeaders(List(
                    Parameter()
                      .withName("Time-Ago")
                      .withSchema(ScalarShape().withDataType("integer"))
                      .withRequired(true)
                  )),
                Response()
                  .withName("404")
                  .withStatusCode("404")
                  .withDescription("Not found!")
                  .withPayloads(List(
                    Payload().withMediaType("application/json").withSchema(ScalarShape().withDataType("string")),
                    Payload().withMediaType("application/xml").withSchema(ScalarShape().withDataType("string"))
                  ))
              ))
          ))
    )

    val api = WebApi()
      .withName("API")
      .withBasePath("/some/uri")
      .withEndPoints(endpoints)

    assertFixture(api, "operation-response.json", OasJsonHint)
  }

  test("generate partial succeed") {
    val api = WebApi()
      .withName("test")
      .withHost("api.example.com")
      .withSchemes(List("http", "https"))
      .withBasePath("/path")
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withProvider(Organization().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto"))
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))

    assertFixture(api, "partialExample.raml", RamlYamlHint)
  }

  test("generate partial json") {

    val api = WebApi()
      .withName("test")
      .withDescription("testDescription")
      .withHost("api.example.com")
      .withSchemes(List("http", "https"))
      .withBasePath("http://api.example.com/path")
      .withContentType(List("application/json"))
      .withAccepts(List("application/json"))
      .withVersion("1.1")
      .withTermsOfService("terminos")
      .withProvider(Organization().withUrl("urlContact").withName("nameContact").withEmail("emailContact"))
      .withLicense(License().withUrl("urlLicense").withName("nameLicense"))
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))

    assertFixture(api, "completeExample.json", OasJsonHint)
  }

  test("shape example test") {

    assertFixture(WebApi(), "example-types.raml", RamlYamlHint)
  }

  test("Generate example with types") {

    val api = WebApi()
      .withName("test title")
      .withDescription("test description")
      .withHost("api.example.com")
      .withSchemes(List("http", "https"))
      .withBasePath("/path")
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withTermsOfService("terms of service")

    val operation = api
      .withEndPoint("/level-zero")
      .withName("One display name")
      .withDescription("and this description!")
      .withOperation("get")
      .withName("Some title")

    val request = operation
      .withRequest()

    //shape of param1
    val param1Shape = request
      .withQueryParameter("param1")
      .withDescription("Some descr")
      .withBinding("query")
      .withRequired(true)
      .withObjectSchema("schema")

    param1Shape
      .withDescription("Some descr")
      .withClosed(false)
      .withProperty("name")
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    param1Shape
      .withProperty("lastName")
      .withScalarSchema("lastName")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val address = param1Shape.withProperty("address").withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("city")
      .withScalarSchema("city")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address.withProperty("postal").withScalarSchema("postal").withDataType("http://www.w3.org/2001/XMLSchema#integer")
    //shape of param1

    //shape of param2
    request
      .withQueryParameter("param2")
      .withBinding("query")
      .withRequired(false)
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    //param3 typeless
    request.withQueryParameter("param3").withRequired(true).withBinding("query").withDescription("typeless")

    //headers
    //header type string
    request
      .withHeader("Header-One")
      .withBinding("header")
      .withRequired(false)
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    //header with object type
    val header2Type =
      request.withHeader("header-two").withRequired(true).withBinding("header").withObjectSchema("schema")
    header2Type
      .withClosed(false)
      .withProperty("number")
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    //body operation payload
    request
      .withPayload()
      .withMediaType("application/raml")
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    //payload of body operation with object
    request
      .withPayload()
      .withMediaType("application/json")
      .withObjectSchema("schema")
      .withClosed(false)
      .withProperty("howmuch")
      .withScalarSchema("howmuch")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    //responses

    val default = operation
      .withResponse("200")

    default
      .withPayload()
      .withObjectSchema("default")
      .withClosed(false)
      .withProperty("invented")
      .withScalarSchema("invented")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    default
      .withPayload()
      .withMediaType("application/xml")
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    operation
      .withResponse("404")
      .withPayload()
      .withScalarSchema("default")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    assertFixture(api, "example-types.raml", RamlYamlHint)
  }

  private def assertField(field: Field, actual: Any, expected: Any): Unit =
    if (expected != actual) {
      expected match {
        case obj: AmfObject =>
      }
      fail(s"Expected $expected but $actual found for field $field")
    }

  private def assertFixture(expected: WebApi, file: String, hint: Hint): Future[Assertion] = {

    AMFCompiler(basePath + file, platform, hint)
      .build()
      .map { unit =>
        val actual = unit.asInstanceOf[Document].encodes
        AmfObjectMatcher(expected).assert(actual)
        Succeeded
      }
  }
}
