package amf.maker

import amf.common.{AmfObjectTestMatcher, ListAssertions}
import amf.compiler.CompilerTestBuilder
import amf.core.metamodel.Field
import amf.core.model.document.Document
import amf.core.model.domain.AmfObject
import amf.core.remote.{AmfJsonHint, Hint, OasJsonHint, RamlYamlHint}
import amf.plugins.domain.shapes.models.DomainExtensions.propertyShapeToPropertyShape
import amf.plugins.domain.shapes.models.{AnyShape, CreativeWork, ScalarShape, XMLSerializer}
import amf.plugins.domain.webapi.models.api.WebApi
import amf.plugins.domain.webapi.models._
import org.scalatest.{Assertion, AsyncFunSuite, Succeeded}

import scala.concurrent.{ExecutionContext, Future}

trait WebApiMakerTest extends AsyncFunSuite with CompilerTestBuilder with ListAssertions with AmfObjectTestMatcher {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://amf-client/shared/src/test/resources/maker/"

  test("Generate complete web api instance") {

    val api = WebApi()
      .withName("test")
      .withDescription("testDescription")
      .withSchemes(List("http", "https"))
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withTermsOfService("terminos")
      .withProvider(Organization().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto"))
      .withLicense(License().withUrl("urlLicense").withName("nameLicense"))
    api.withServer("http://api.example.com/path")
    api.withDocumentationUrl("urlExternalDocs").withDescription("descriptionExternalDocs")

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
      .withEndPoints(endpoints)

    api.withServer("/some/base/uri")

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
      .withEndPoints(endpoints)
    api.withServer("/some/base/uri")

    assertFixture(api, "nested-endpoints.json", OasJsonHint)
  }

  test("WebApi with multiple operations - RAML.") {
    val api = WebApi()
      .withName("API")
    api.withServer("/some/base/uri")
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
    api.withServer("/some/base/uri")

    api.withEndPoint("/levelzero").withName("Name")

    val endpointOne = api
      .withEndPoint("/levelzero/level-one")
      .withName("One display name")
      .withDescription("and this description!")

    val operationGet = endpointOne.withOperation("get")
    operationGet
      .withName("Some title")
      .withDescription("Some description")
      .withOperationId("Some title")
      .withDeprecated(true)
      .withSummary("This is a summary")
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))
      .withSchemes(List("http", "https"))
    endpointOne
      .withOperation("post")
      .withName("Some title")
      .withOperationId("Some title")
      .withDescription("Some description")
      .withDocumentation(CreativeWork().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs"))
      .withSchemes(List("http", "https"))

    assertFixture(api, "endpoint-operations.json", OasJsonHint)
  }

  test("Parameters - RAML.") {

    val endpoints = List(
      EndPoint()
        .withPath("/levelzero/some{two}")
        .withParameters(
          List(
            Parameter()
              .withName("two")
              .withParameterName("two")
              .withRequired(false)
              .withBinding("path")
              .withSchema(ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
          )),
      EndPoint()
        .withPath("/levelzero/some{two}/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withParameters(
          Seq(
            Parameter()
              .withName("two")
              .withParameterName("two")
              .withRequired(false)
              .withBinding("path")
              .withSchema(ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
          ))
        .withOperations(List(
          Operation()
            .withMethod("get")
            .withName("Some title")
            .withRequest(Request()
              .withQueryParameters(List(
                Parameter()
                  .withName("param1")
                  .withParameterName("param1")
                  .withDescription("Some descr")
                  .withRequired(true)
                  .withBinding("query")
                  .withSchema(ScalarShape()
                    .withName("schema")
                    .withDescription("Some descr")
                    .withDataType("http://www.w3.org/2001/XMLSchema#string")),
                Parameter()
                  .withName("param2")
                  .withParameterName("param2")
                  .withSchema(ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  .withRequired(false)
                  .withBinding("query")
              ))),
          Operation()
            .withMethod("post")
            .withName("Some title")
            .withDescription("Some description")
            .withRequest(Request()
              .withHeaders(List(
                Parameter()
                  .withName("Header-One")
                  .withParameterName("Header-One")
                  .withRequired(false)
                  .withBinding("header")
                  .withSchema(ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
              )))
        ))
    )
    val api = WebApi()
      .withName("API")
      .withEndPoints(endpoints)

    api
      .withServer("/some/{one}/uri")
      .withVariables(
        List(
          Parameter()
            .withName("one")
            .withParameterName("one")
            .withRequired(true)
            .withDescription("One base uri param")
            .withBinding("path")
            .withSchema(ScalarShape()
              .withName("schema")
              .withDescription("One base uri param")
              .withDataType("http://www.w3.org/2001/XMLSchema#string"))))

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
        .withPayloads(
          List(Payload()
            .withName("paramName")
            .withMediaType("application/json")
            .withSchema(ScalarShape().withName("paramName").withDataType("http://www.w3.org/2001/XMLSchema#integer"))))
        .withOperations(List(
          Operation()
            .withMethod("get")
            .withName("Some title")
            .withOperationId("Some title")
            .withRequest(
              Request()
                .withQueryParameters(
                  List(
                    Parameter()
                      .withName("param1")
                      .withParameterName("param1")
                      .withDescription("Some descr")
                      .withRequired(true)
                      .withBinding("query")
                      .withSchema(ScalarShape()
                        .withName("schema")
                        .withDescription("Some descr")
                        .withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  ))
                .withHeaders(List(Parameter()
                  .withName("param2?")
                  .withParameterName("param2?")
                  .withSchema(ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  .withRequired(false)
                  .withBinding("header")))
                .withPayloads(List(Payload()
                  .withName("otherParamName")
                  .withSchema(
                    ScalarShape().withName("otherParamName").withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  .withMediaType("application/xml")))
            ),
          Operation()
            .withMethod("post")
            .withName("Some title")
            .withOperationId("Some title")
            .withDescription("Some description")
            .withRequest(
              Request()
                .withHeaders(
                  List(
                    Parameter()
                      .withName("Header-One")
                      .withParameterName("Header-One")
                      .withRequired(false)
                      .withBinding("header")
                      .withSchema(
                        ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  ))
            )
        ))
    )
    val api = WebApi()
      .withName("API")
      .withEndPoints(endpoints)

    api.withServer("/some/base/uri")

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
                .withPayloads(List(Payload()
                  .withMediaType("application/json")
                  .withSchema(ScalarShape().withName("schema")))))
              .withResponses(List(
                Response()
                  .withDescription("200 descr")
                  .withStatusCode("200")
                  .withName("200")
                  .withHeaders(
                    List(
                      Parameter()
                        .withName("Time-Ago")
                        .withParameterName("Time-Ago")
                        .withBinding("header")
                        .withSchema(
                          ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#integer"))
                        .withRequired(true)
                    )),
                Response()
                  .withName("404")
                  .withStatusCode("404")
                  .withDescription("Not found!")
                  .withPayloads(List(
                    Payload()
                      .withMediaType("application/json")
                      .withSchema(
                        ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string")),
                    Payload()
                      .withMediaType("application/xml")
                      .withSchema(
                        ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  ))
              ))
          ))
    )

    val api = WebApi()
      .withName("API")
      .withEndPoints(endpoints)

    api.withServer("/some/uri")

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
              .withOperationId("Some title")
              .withRequest(
                Request()
                  .withPayloads(List(Payload()
                    .withMediaType("application/json")
                    .withSchema(
                      ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#integer")))))
              .withResponses(List(
                Response()
                  .withDescription("200 descr")
                  .withStatusCode("200")
                  .withName("200")
                  .withHeaders(
                    List(
                      Parameter()
                        .withName("Time-Ago")
                        .withBinding("header")
                        .withSchema(
                          ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#integer"))
                        .withRequired(true)
                    )),
                Response()
                  .withName("404")
                  .withStatusCode("404")
                  .withDescription("Not found!")
                  .withPayloads(List(
                    Payload()
                      .withMediaType("application/json")
                      .withSchema(
                        ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string")),
                    Payload()
                      .withMediaType("application/xml")
                      .withSchema(
                        ScalarShape().withName("schema").withDataType("http://www.w3.org/2001/XMLSchema#string"))
                  ))
              ))
          ))
    )

    val api = WebApi()
      .withName("API")
      .withEndPoints(endpoints)
    api.withServer("/some/uri")

    assertFixture(api, "operation-response.json", OasJsonHint)
  }

  test("generate partial succeed") {
    val api = WebApi()
      .withName("test")
      .withSchemes(List("http", "https"))
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withProvider(Organization().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto"))

    api.withServer("http://api.example.com/path")

    api.withDocumentationUrl("urlExternalDocs").withDescription("descriptionExternalDocs")

    assertFixture(api, "partialExample.raml", RamlYamlHint)
  }

  test("generate partial json") {

    val api = WebApi()
      .withName("test")
      .withDescription("testDescription")
      .withSchemes(List("http", "https"))
      .withContentType(List("application/json"))
      .withAccepts(List("application/json"))
      .withVersion("1.1")
      .withTermsOfService("terminos")
      .withProvider(Organization().withUrl("urlContact").withName("nameContact").withEmail("emailContact"))
      .withLicense(License().withUrl("urlLicense").withName("nameLicense"))

    api.withServer("api.example.com/path")
    api.withDocumentationUrl("urlExternalDocs").withDescription("descriptionExternalDocs")

    assertFixture(api, "completeExample.json", OasJsonHint)
  }

  test("Generate raml example with types") {

    assertFixture(webApiWithTypes(), "example-types.raml", RamlYamlHint)
  }

  test("Generate amf example with types") {
    val data = webApiWithTypes()
    assertFixture(data, "example-types.raml.jsonld", AmfJsonHint)
  }

  test("Generate oas example with types") {

    val api = WebApi()
      .withName("test title")
      .withDescription("test description")
      .withSchemes(List("http", "https"))
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withTermsOfService("terms of service")

    api.withServer("api.example.com/path")

    val operation = api
      .withEndPoint("/level-zero")
      .withName("One display name")
      .withDescription("and this description!")
      .withOperation("get")
      .withName("Some title")
      .withOperationId("Some title")

    val request = operation
      .withRequest()

    // shape of param1
    val payloadParam1 = request.withPayload()

    val objectParam1 = payloadParam1
      .withName("param1")
      .withObjectSchema("param1")
      .withClosed(false)

    objectParam1
      .withProperty("name")
      .withPath("http://a.ml/vocabularies/data#name")
      .withMinCount(0)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    objectParam1
      .withProperty("lastName")
      .withPath("http://a.ml/vocabularies/data#lastName")
      .withMinCount(0)
      .withScalarSchema("lastName")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val address =
      objectParam1
        .withProperty("address")
        .withPath("http://a.ml/vocabularies/data#address")
        .withMinCount(0)
        .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("city")
      .withPath("http://a.ml/vocabularies/data#city")
      .withMinCount(0)
      .withScalarSchema("city")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("postal")
      .withPath("http://a.ml/vocabularies/data#postal")
      .withMinCount(0)
      .withScalarSchema("postal")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")
    //shape of param1

    //shape of param2
    request
      .withQueryParameter("param2")
      .withParameterName("param2")
      .withBinding("query")
      .withRequired(false)
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    //param3 typeless
    request
      .withQueryParameter("param3")
      .withParameterName("param3")
      .withRequired(false)
      .withBinding("query")
      .withSchema(AnyShape())
      .withDescription("typeless")

    //headers
    //header type string
    request
      .withHeader("Header-One")
      .withParameterName("Header-One")
      .withBinding("header")
      .withRequired(false)
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    //header with object type
    val header2Type =
      request
        .withHeader("header-two")
        .withParameterName("header-two")
        .withRequired(false)
        .withBinding("header")
        .withScalarSchema("schema")
        .withDataType("http://www.w3.org/2001/XMLSchema#string")

    //body operation payload
    request
      .withPayload(Some("application/raml"))
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    //payload of body operation with object
    request
      .withPayload(Some("application/json"))
      .withObjectSchema("schema")
      .withClosed(false)
      .withXMLSerialization(
        XMLSerializer()
          .withAttribute(true)
          .withWrapped(false)
          .withName("Xml-name")
          .withNamespace("xsd")
          .withPrefix("pref"))
      .withProperty("howmuch")
      .withPath("http://a.ml/vocabularies/data#howmuch")
      .withMinCount(0)
      .withScalarSchema("howmuch")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    //responses

    val default = operation
      .withResponse("default")
      .withStatusCode("default")

    default
      .withPayload(Some("application/xml"))
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    default
      .withPayload()
      .withObjectSchema("default")
      .withClosed(false)
      .withProperty("invented")
      .withPath("http://a.ml/vocabularies/data#invented")
      .withMinCount(0)
      .withScalarSchema("invented")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    operation
      .withResponse("404")
      .withPayload()
      .withScalarSchema("default")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    api
      .withEndPoint("/scalar_array")
      .withOperation("get")
      .withOperationId("scalar_array")
      .withName("scalar_array")
      .withRequest()
      .withPayload(None)
      .withName("scalar_array")
      .withArraySchema("scalar_array")
      .withMinItems(3)
      .withMaxItems(10)
      .withUniqueItems(true)
      .withScalarItems()
      .withName("items")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    api
      .withEndPoint("/object_array")
      .withOperation("get")
      .withName("object_array")
      .withOperationId("object_array")
      .withRequest()
      .withPayload(None)
      .withName("object_array")
      .withArraySchema("object_array")
      .withNodeItems()
      .withClosed(false)
      .withName("items")

    assertFixture(api, "example-types.json", OasJsonHint)
  }

  private def assertField(field: Field, actual: Any, expected: Any): Unit =
    if (expected != actual) {
      expected match {
        case obj: AmfObject =>
      }
      fail(s"Expected $expected but $actual found for field $field")
    }

  private def assertFixture(expected: WebApi,
                            file: String,
                            hint: Hint,
                            overridedPath: Option[String] = None): Future[Assertion] = {

    build(overridedPath.getOrElse(basePath) + file, hint)
      .map { unit =>
        val actual = unit.asInstanceOf[Document].encodes
        AmfObjectMatcher(expected).assert(actual)
        Succeeded
      }
  }

  private def webApiWithTypes(): WebApi = {
    val api = WebApi()
      .withName("test title")
      .withDescription("test description")
      .withSchemes(List("http", "https"))
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withTermsOfService("terms of service")

    api.withServer("api.example.com/path")

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
      .withParameterName("param1")
      .withDescription("Some descr")
      .withBinding("query")
      .withRequired(true)
      .withObjectSchema("schema")

    param1Shape
      .withDescription("Some descr")
      .withClosed(false)
      .withProperty("name")
      .withMinCount(1)
      .withPath("http://a.ml/vocabularies/data#name")
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    param1Shape
      .withProperty("lastName")
      .withMinCount(1)
      .withPath("http://a.ml/vocabularies/data#lastName")
      .withScalarSchema("lastName")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val address = param1Shape
      .withProperty("address")
      .withMinCount(1)
      .withPath("http://a.ml/vocabularies/data#address")
      .withObjectRange("address")
    address
      .withClosed(false)
      .withProperty("city")
      .withPath("http://a.ml/vocabularies/data#city")
      .withMinCount(1)
      .withScalarSchema("city")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    address
      .withProperty("postal")
      .withMinCount(1)
      .withPath("http://a.ml/vocabularies/data#postal")
      .withScalarSchema("postal")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")
    //shape of param1

    //shape of param2
    request
      .withQueryParameter("param2")
      .withParameterName("param2")
      .withBinding("query")
      .withRequired(false)
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    //param3 typeless , default type its string?
    request
      .withQueryParameter("param3")
      .withParameterName("param3")
      .withRequired(true)
      .withBinding("query")
      .withDescription("typeless")
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
      .withDescription("typeless")

    //headers
    //header type string
    request
      .withHeader("Header-One")
      .withParameterName("Header-One")
      .withBinding("header")
      .withRequired(false)
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
      .withXMLSerialization(
        XMLSerializer()
          .withAttribute(true)
          .withWrapped(false)
          .withName("Xml-name")
          .withNamespace("xsd")
          .withPrefix("pref"))

    //header with object type
    val header2Type =
      request
        .withHeader("header-two")
        .withParameterName("header-two")
        .withRequired(true)
        .withBinding("header")
        .withObjectSchema("schema")
    header2Type
      .withClosed(false)
      .withProperty("number")
      .withPath("http://a.ml/vocabularies/data#number")
      .withMinCount(1)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    //body operation payload
    request
      .withPayload(Some("application/raml"))
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    //payload of body operation with object
    request
      .withPayload(Some("application/json"))
      .withObjectSchema("schema")
      .withClosed(false)
      .withProperty("howmuch")
      .withPath("http://a.ml/vocabularies/data#howmuch")
      .withMinCount(1)
      .withScalarSchema("howmuch")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    //responses

    val default = operation
      .withResponse("200")

    default
      .withPayload(Some("application/json"))
      .withObjectSchema("schema")
      .withClosed(false)
      .withProperty("invented")
      .withPath("http://a.ml/vocabularies/data#invented")
      .withMinCount(1)
      .withScalarSchema("invented")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    default
      .withPayload(Some("application/xml"))
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    operation
      .withResponse("404")
      .withPayload()
      .withScalarSchema("default")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    // array operations

    api
      .withEndPoint("/scalar_array")
      .withOperation("get")
      .withName("scalar_array")
      .withRequest()
      .withPayload(Some("application/json"))
      .withArraySchema("schema")
      .withDisplayName("scalar_array")
      .withMinItems(3)
      .withMaxItems(10)
      .withUniqueItems(true)
      .withScalarItems()
      .withName("items")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    api
      .withEndPoint("/object_array")
      .withOperation("get")
      .withName("object_array")
      .withRequest()
      .withPayload(Some("application/json"))
      .withArraySchema("schema")
      .withDisplayName("object_array")
      .withNodeItems()
      .withClosed(false)
      .withName("items")

    api
  }

  // TODO
  private def webApiWithDependencyTypes(): WebApi = {
    val api = WebApi()
      .withName("test title")
      .withDescription("test description")
      .withSchemes(List("http", "https"))
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withTermsOfService("terms of service")
      .withId("amf-client/shared/src/test/resources/maker/types-dependency.raml#/web-api")

    api.withServer("api.example.com/path")

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
      .withMinCount(1)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    param1Shape
      .withProperty("lastName")
      .withMinCount(1)
      .withScalarSchema("lastName")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val address = param1Shape.withProperty("address").withMinCount(1).withObjectRange("address")
    val city = address
      .withClosed(false)
      .withProperty("city")
    city
      .withMinCount(1)
      .withScalarSchema("city")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val street = address
      .withProperty("street")
    street
      .withMinCount(1)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val number = address
      .withProperty("number")
    number
      .withMinCount(1)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")
    val postal = address
      .withProperty("postal")
    postal
      .withMinCount(1)
      .withScalarSchema("postal")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    address
      .withDependency()
      .withPropertySource(city.id)
      .withPropertyTarget(Seq(postal.id))
    address
      .withDependency()
      .withPropertySource(street.id)
      .withPropertyTarget(Seq(number.id, postal.id, city.id))
    address
      .withDependency()
      .withPropertySource(number.id)
      .withPropertyTarget(Seq(street.id))

    //body operation payload
    val bodySchema = request
      .withPayload(Some("application/raml"))
      .withObjectSchema("schema")
      .withClosed(false)
    val credit_card = bodySchema
      .withProperty("credit_card")
    credit_card
      .withMinCount(1)
      .withScalarSchema("credit_card")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val creditCity = bodySchema
      .withProperty("city")
    creditCity
      .withMinCount(1)
      .withScalarSchema("city")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    val creditStreet = bodySchema
      .withProperty("street")
    creditStreet
      .withMinCount(1)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    val creditNumber = bodySchema
      .withProperty("number")
    creditNumber
      .withMinCount(1)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val creditPostal = bodySchema
      .withProperty("postal")
    creditPostal
      .withMinCount(1)
      .withScalarSchema("postal")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")
    //payload of body operation with object

    bodySchema
      .withDependency()
      .withPropertySource(credit_card.id)
      .withPropertyTarget(Seq(creditCity.id, creditPostal.id))
    bodySchema
      .withDependency()
      .withPropertySource(creditStreet.id)
      .withPropertyTarget(Seq(creditNumber.id, creditPostal.id, creditCity.id))
    bodySchema
      .withDependency()
      .withPropertySource(creditNumber.id)
      .withPropertyTarget(Seq(creditStreet.id))

    //responses

    val default = operation
      .withResponse("200")
    default
      .withPayload(Some("application/xml"))
      .withScalarSchema("schema")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    operation
      .withResponse("404")
      .withPayload()
      .withScalarSchema("default")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    api
  }

  private def oasWebApiWithDependencyTypes(): WebApi = {
    val api = WebApi()
      .withName("test title")
      .withDescription("test description")
      .withSchemes(List("http", "https"))
      .withContentType(List("application/yaml"))
      .withAccepts(List("application/yaml"))
      .withVersion("1.1")
      .withTermsOfService("terms of service")
      .withId("amf-client/shared/src/test/resources/maker/types-dependency.json#/web-api")

    api.withServer("api.example.com/path")

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
      .withPayload()
      .withObjectSchema("schema")

    param1Shape
      .withClosed(false)
      .withProperty("name")
      .withMinCount(0)
      .withScalarSchema("name")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    param1Shape
      .withProperty("lastName")
      .withMinCount(0)
      .withScalarSchema("lastName")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val address = param1Shape.withProperty("address").withMinCount(0).withObjectRange("address")
    val city = address
      .withClosed(false)
      .withProperty("city")
    city
      .withMinCount(0)
      .withScalarSchema("city")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val street = address
      .withProperty("street")
    street
      .withMinCount(0)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")
    val number = address
      .withProperty("number")
    number
      .withMinCount(0)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")
    val postal = address
      .withProperty("postal")
    postal
      .withMinCount(0)
      .withScalarSchema("postal")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    address
      .withDependency()
      .withPropertySource(city.id)
      .withPropertyTarget(Seq(postal.id))
    address
      .withDependency()
      .withPropertySource(street.id)
      .withPropertyTarget(Seq(number.id, postal.id, city.id))
    address
      .withDependency()
      .withPropertySource(number.id)
      .withPropertyTarget(Seq(street.id))

    //body operation payload
    val bodySchema = request
      .withPayload(Some("application/raml"))
      .withObjectSchema("schema")
      .withClosed(false)
    val credit_card = bodySchema
      .withProperty("credit_card")
    credit_card
      .withMinCount(0)
      .withScalarSchema("credit_card")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val creditCity = bodySchema
      .withProperty("city")
    creditCity
      .withMinCount(0)
      .withScalarSchema("city")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    val creditStreet = bodySchema
      .withProperty("street")
    creditStreet
      .withMinCount(0)
      .withScalarSchema("street")
      .withDataType("http://www.w3.org/2001/XMLSchema#string")

    val creditNumber = bodySchema
      .withProperty("number")
    creditNumber
      .withMinCount(0)
      .withScalarSchema("number")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")

    val creditPostal = bodySchema
      .withProperty("postal")
    creditPostal
      .withMinCount(0)
      .withScalarSchema("postal")
      .withDataType("http://www.w3.org/2001/XMLSchema#integer")
    //payload of body operation with object

    bodySchema
      .withDependency()
      .withPropertySource(credit_card.id)
      .withPropertyTarget(Seq(creditCity.id, creditPostal.id))
    bodySchema
      .withDependency()
      .withPropertySource(creditStreet.id)
      .withPropertyTarget(Seq(creditNumber.id, creditPostal.id, creditCity.id))
    bodySchema
      .withDependency()
      .withPropertySource(creditNumber.id)
      .withPropertyTarget(Seq(creditStreet.id))

    //responses

    api
  }
}
