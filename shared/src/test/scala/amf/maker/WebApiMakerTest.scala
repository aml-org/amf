package amf.maker

import amf.builder._
import amf.common.ListAssertions
import amf.compiler.AMFCompiler
import amf.document.Document
import amf.domain.WebApi
import amf.metadata.Field
import amf.metadata.domain.WebApiModel._
import amf.remote.{AmfJsonLdHint, Hint, OasJsonHint, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class WebApiMakerTest extends AsyncFunSuite with PlatformSecrets with ListAssertions {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/maker/"

  test("Generate complete web api instance") {

    val fixture = List(
      (Name, "test"),
      (Description, "testDescription"),
      (Host, "http://api.example.com/path"),
      (Schemes, List("http", "https")),
      (BasePath, "/path"),
      (ContentType, "application/yaml"),
      (Accepts, "application/yaml"),
      (Version, "1.1"),
      (TermsOfService, "terminos"),
      (Provider,
       OrganizationBuilder().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto").build),
      (License, LicenseBuilder().withUrl("urlLicense").withName("nameLicense").build),
      (Documentation,
       CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
    )

    assertFixture(fixture, "completeExample.raml", RamlYamlHint)
  }

  test("WebApi with nested endpoints - RAML.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero")
        .build,
      EndPointBuilder()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .build,
      EndPointBuilder().withPath("/levelzero/another-level-one").withName("some other display name").build,
      EndPointBuilder().withPath("/another-levelzero").withName("Root name").build
    )
    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/base/uri"),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "nested-endpoints.raml", RamlYamlHint)
  }

  test("WebApi with nested endpoints - OAS.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero")
        .withName("Name")
        .build,
      EndPointBuilder()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .build,
      EndPointBuilder().withPath("/levelzero/another-level-one").withName("some other display name").build,
      EndPointBuilder().withPath("/another-levelzero").withName("Root name").build
    )
    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/base/uri"),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "nested-endpoints.json", OasJsonHint)
  }

  test("WebApi with multiple operations - RAML.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero")
        .build,
      EndPointBuilder()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(List(
          OperationBuilder()
            .withMethod("get")
            .withName("Some title")
            .withDescription("Some description")
            .withDeprecated(true)
            .withSummary("This is a summary")
            .withDocumentation(
              CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
            .withSchemes(List("http", "https"))
            .build,
          OperationBuilder()
            .withMethod("post")
            .withName("Some title")
            .withDescription("Some description")
            .withDocumentation(
              CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
            .withSchemes(List("http", "https"))
            .build
        ))
        .build
    )
    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/base/uri"),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "endpoint-operations.raml", RamlYamlHint)
  }

  test("WebApi with multiple operations - OAS.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero")
        .withName("Name")
        .build,
      EndPointBuilder()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(List(
          OperationBuilder()
            .withMethod("get")
            .withName("Some title")
            .withDescription("Some description")
            .withDeprecated(true)
            .withSummary("This is a summary")
            .withDocumentation(
              CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
            .withSchemes(List("http", "https"))
            .build,
          OperationBuilder()
            .withMethod("post")
            .withName("Some title")
            .withDescription("Some description")
            .withDocumentation(
              CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
            .withSchemes(List("http", "https"))
            .build
        ))
        .build
    )
    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/base/uri"),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "endpoint-operations.json", OasJsonHint)
  }

  test("Parameters - RAML.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero/some{two}")
        .withParameters(List(ParameterBuilder().withName("two").withRequired(false).build))
        .build,
      EndPointBuilder()
        .withPath("/levelzero/some{two}/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(List(
          OperationBuilder()
            .withMethod("get")
            .withName("Some title")
            .withRequest(RequestBuilder()
              .withQueryParameters(List(
                ParameterBuilder().withName("param1").withDescription("Some descr").withRequired(true).build,
                ParameterBuilder().withName("param2?").withSchema("string").withRequired(false).build
              ))
              .build)
            .build,
          OperationBuilder()
            .withMethod("post")
            .withName("Some title")
            .withDescription("Some description")
            .withRequest(RequestBuilder()
              .withHeaders(List(
                ParameterBuilder().withName("Header-One").withRequired(false).build
              ))
              .build)
            .build
        ))
        .build
    )
    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/{one}/uri"),
      (BaseUriParameters,
       List(ParameterBuilder().withName("one").withRequired(true).withDescription("One base uri param").build)),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "operation-request.raml", RamlYamlHint)
  }

  test("Parameters - OAS.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero")
        .withName("Name")
        .build,
      EndPointBuilder()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(List(
          OperationBuilder()
            .withMethod("get")
            .withName("Some title")
            .withRequest(
              RequestBuilder()
                .withQueryParameters(
                  List(
                    ParameterBuilder()
                      .withName("param1")
                      .withDescription("Some descr")
                      .withRequired(true)
                      .withBinding("query")
                      .build
                  ))
                .withHeaders(List(ParameterBuilder()
                  .withName("param2?")
                  .withSchema("string")
                  .withRequired(false)
                  .withBinding("header")
                  .build))
                .withPayloads(List(PayloadBuilder().withSchema("string").withMediaType("application/xml").build))
                .build)
            .build,
          OperationBuilder()
            .withMethod("post")
            .withName("Some title")
            .withDescription("Some description")
            .withRequest(
              RequestBuilder()
                .withHeaders(List(
                  ParameterBuilder().withName("Header-One").withRequired(false).withBinding("header").build
                ))
                .withPayloads(List(PayloadBuilder().withSchema("number").withMediaType("application/json").build))
                .build)
            .build
        ))
        .build
    )
    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/base/uri"),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "operation-request.json", OasJsonHint)
  }

  test("Responses - RAML.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero")
        .build,
      EndPointBuilder()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(
          List(
            OperationBuilder()
              .withMethod("get")
              .withName("Some title")
              .withRequest(RequestBuilder()
                .withPayloads(List(PayloadBuilder().withMediaType("application/json").build))
                .build)
              .withResponses(List(
                ResponseBuilder()
                  .withDescription("200 descr")
                  .withStatusCode("200")
                  .withName("200")
                  .withHeaders(List(
                    ParameterBuilder().withName("Time-Ago").withSchema("integer").withRequired(true).build
                  ))
                  .build,
                ResponseBuilder()
                  .withName("404")
                  .withStatusCode("404")
                  .withDescription("Not found!")
                  .withPayloads(List(PayloadBuilder().withMediaType("application/json").build,
                                     PayloadBuilder().withMediaType("application/xml").build))
                  .build
              ))
              .build))
        .build
    )

    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/uri"),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "operation-response.raml", RamlYamlHint)
  }

  test("Responses - OAS.") {
    val endpoints = List(
      EndPointBuilder()
        .withPath("/levelzero")
        .withName("Name")
        .build,
      EndPointBuilder()
        .withPath("/levelzero/level-one")
        .withName("One display name")
        .withDescription("and this description!")
        .withOperations(
          List(
            OperationBuilder()
              .withMethod("get")
              .withName("Some title")
              .withRequest(RequestBuilder()
                .withPayloads(List(PayloadBuilder().withMediaType("application/json").build))
                .build)
              .withResponses(List(
                ResponseBuilder()
                  .withDescription("200 descr")
                  .withStatusCode("200")
                  .withName("default")
                  .withHeaders(List(
                    ParameterBuilder().withName("Time-Ago").withSchema("integer").withRequired(true).build
                  ))
                  .build,
                ResponseBuilder()
                  .withName("404")
                  .withStatusCode("404")
                  .withDescription("Not found!")
                  .withPayloads(List(PayloadBuilder().withMediaType("application/json").build,
                                     PayloadBuilder().withMediaType("application/xml").build))
                  .build
              ))
              .build))
        .build
    )

    val fixture = List(
      (Name, "API"),
      (BasePath, "/some/uri"),
      (EndPoints, endpoints)
    )

    assertFixture(fixture, "operation-response.json", OasJsonHint)
  }

  test("generate partial succeed") {
    val fixture = List(
      (Name, "test"),
      (Description, null),
      (Host, "http://api.example.com/path"),
      (Schemes, List("http", "https")),
      (BasePath, "/path"),
      (ContentType, "application/yaml"),
      (Accepts, "application/yaml"),
      (Version, "1.1"),
      (TermsOfService, null),
      (Provider,
       OrganizationBuilder().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto").build),
      (Documentation,
       CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
    )

    assertFixture(fixture, "partialExample.raml", RamlYamlHint)
  }

  ignore("basic jsonld example") {
    val fixture = List(
      (Name, "test"),
      (Description, null),
      (Host, "api.example.com"),
      (Schemes, List("http", "https")),
      (BasePath, null),
      (TermsOfService, null),
      (Provider, null),
      (License, null),
      (Documentation, null)
    )

    assertFixture(fixture, "basicExample.jsonld", AmfJsonLdHint)
  }

  test("generate partial json") {

    val fixture = List(
      (Name, "test"),
      (Description, "testDescription"),
      (Host, "api.example.com"),
      (Schemes, List("http", "https")),
      (BasePath, "http://api.example.com/path"),
      (ContentType, "application/json"),
      (Accepts, "application/json"),
      (Version, "1.1"),
      (TermsOfService, "terminos"),
      (Provider, OrganizationBuilder().withUrl("urlContact").withName("nameContact").withEmail("emailContact").build),
      (License, LicenseBuilder().withUrl("urlLicense").withName("nameLicense").build),
      (Documentation,
       CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
    )

    assertFixture(fixture, "completeExample.json", OasJsonHint)
  }

  test("edit complete raml") {

    val before = List(
      (Name, "test"),
      (Description, "testDescription"),
      (Host, "http://api.example.com/path"),
      (Schemes, List("http", "https")),
      (BasePath, "/path"),
      (ContentType, "application/yaml"),
      (Accepts, "application/yaml"),
      (Version, "1.1"),
      (TermsOfService, "terminos"),
      (Provider,
       OrganizationBuilder().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto").build),
      (License, LicenseBuilder().withUrl("urlLicense").withName("nameLicense").build),
      (Documentation,
       CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
    )

    val after = List(
      (Name, "test"),
      (Description, "changed"),
      (Host, "http://api.example.com/path"),
      (Schemes, List("http", "https")),
      (BasePath, "/path"),
      (ContentType, "application/yaml"),
      (Accepts, "application/yaml"),
      (Version, "1.1"),
      (TermsOfService, "terminos"),
      (Provider,
       OrganizationBuilder().withUrl("urlContacto").withName("nombreContacto").withEmail("mailContacto").build),
      (License, LicenseBuilder().withUrl("urlLicense").withName("changed").build),
      (Documentation,
       CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
    )

    AMFCompiler(basePath + "completeExample.raml", platform, RamlYamlHint)
      .build()
      .map { unit =>
        val api = unit.asInstanceOf[Document].encodes
        assertWebApiValues(api, before)
        val builder = api.toBuilder
        builder.withDescription("changed")
        val license = api.license.toBuilder
        license.withName("changed")
        builder.withLicense(license.build)
        assertWebApiValues(builder.build, after)
      }
  }

  test("edit complete json") {
    val before = List(
      (Name, "test"),
      (Description, "testDescription"),
      (Host, "api.example.com"),
      (Schemes, List("http", "https")),
      (BasePath, "http://api.example.com/path"),
      (ContentType, "application/json"),
      (Accepts, "application/json"),
      (Version, "1.1"),
      (TermsOfService, "terminos"),
      (Provider, OrganizationBuilder().withUrl("urlContact").withName("nameContact").withEmail("emailContact").build),
      (License, LicenseBuilder().withUrl("urlLicense").withName("nameLicense").build),
      (Documentation,
       CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
    )

    val after = List(
      (Name, "test"),
      (Description, "changed"),
      (Host, "api.example.com"),
      (Schemes, List("http", "https")),
      (BasePath, "http://api.example.com/path"),
      (ContentType, "application/json"),
      (Accepts, "application/json"),
      (Version, "1.1"),
      (TermsOfService, "terminos"),
      (Provider, OrganizationBuilder().withUrl("urlContact").withName("nameContact").withEmail("emailContact").build),
      (License, LicenseBuilder().withUrl("urlLicense").withName("changed").build),
      (Documentation,
       CreativeWorkBuilder().withUrl("urlExternalDocs").withDescription("descriptionExternalDocs").build)
    )

    AMFCompiler(basePath + "completeExample.json", platform, OasJsonHint)
      .build()
      .map { unit =>
        val api = unit.asInstanceOf[Document].encodes
        assertWebApiValues(api, before)
        val builder = api.toBuilder
        builder.withDescription("changed")
        val license = api.license.toBuilder
        license.withName("changed")
        builder.withLicense(license.build)
        assertWebApiValues(builder.build, after)
      }
  }

  def assertWebApiValues(api: WebApi, assertions: List[(Field, Any)]): Assertion = {
    assertions.foreach {
      case (Name, expected)              => assertField(Name, api.name, expected)
      case (Description, expected)       => assertField(Description, api.description, expected)
      case (Host, expected)              => assertField(Host, api.host, expected)
      case (Schemes, expected)           => assertField(Schemes, api.schemes, expected)
      case (BasePath, expected)          => assertField(BasePath, api.basePath, expected)
      case (ContentType, expected)       => assertField(ContentType, api.contentType, expected)
      case (Accepts, expected)           => assertField(Accepts, api.accepts, expected)
      case (Version, expected)           => assertField(Version, api.version, expected)
      case (TermsOfService, expected)    => assertField(TermsOfService, api.termsOfService, expected)
      case (Provider, expected)          => assertField(Provider, api.provider, expected)
      case (License, expected)           => assertField(License, api.license, expected)
      case (Documentation, expected)     => assertField(Documentation, api.documentation, expected)
      case (EndPoints, expected)         => assertField(EndPoints, api.endPoints, expected)
      case (BaseUriParameters, expected) => assertField(BaseUriParameters, api.baseUriParameters, expected)
    }
    succeed
  }

  private def assertField(field: Field, actual: Any, expected: Any) =
    if (expected != actual) fail(s"Expected $expected but $actual found for field $field")

  private def assertFixture(fixture: List[(Field, Object)], file: String, hint: Hint): Future[Assertion] = {

    AMFCompiler(basePath + file, platform, hint)
      .build()
      .map { unit =>
        val api = unit.asInstanceOf[Document].encodes
        assertWebApiValues(api, fixture)
      }
  }
}
