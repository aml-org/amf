package amf.maker

import amf.common.ListAssertions
import amf.compiler.AMFCompiler
import amf.model.{CreativeWork, License, Organization, WebApi}
import amf.parser._
import amf.remote.{OasJsonHint, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFlatSpec}
import org.scalatest.Matchers._
import amf.common.Strings.strings
import scala.concurrent.ExecutionContext

class WebApiMakerTest extends AsyncFlatSpec with PlatformSecrets with ListAssertions {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/maker/"

  "generate complete web api instance" should "succeed " in {
    val eventualApi = AMFCompiler(basePath + "completeExample.raml", platform, Some(RamlYamlHint))
      .build()
      .map(t => WebApiMaker(AMFUnit(t._1, basePath + "completeExample.raml", Document, t._2)).make)

    val expected = List(
      ("name", "test"),
      ("description", "testDescription"),
      ("host", "http://api.example.com/path"),
      ("scheme", List("http", "https")),
      ("basePath", "/path"),
      ("contentType", "application/yaml"),
      ("accepts", "application/yaml"),
      ("version", "1.1"),
      ("termsOfService", "terminos"),
      ("provider", new Organization("urlContacto", "nombreContacto", "mailContacto")),
      ("license", new License("urlLicense", "nameLicense")),
      ("documentation", new CreativeWork("urlExternalDocs", "descriptionExternalDocs"))
    )

    eventualApi map { api =>
      assertWebApiValues(api, expected)
    }
  }

  "generate partial " should "succeed " in {
    val eventualApi = AMFCompiler(basePath + "partialExample.raml", platform, Some(RamlYamlHint))
      .build()
      .map(t => WebApiMaker(AMFUnit(t._1, basePath + "completeExample.raml", Document, t._2)).make)

    val expected = List(
      ("name", "test"),
      ("description", null),
      ("host", "http://api.example.com/path"),
      ("scheme", List("http", "https")),
      ("basePath", "/path"),
      ("contentType", "application/yaml"),
      ("accepts", "application/yaml"),
      ("version", "1.1"),
      ("termsOfService", null),
      ("provider", new Organization("urlContacto", "nombreContacto", "mailContacto")),
      ("license", null),
      ("documentation", new CreativeWork("urlExternalDocs", "descriptionExternalDocs"))
    )

    eventualApi map { api =>
      assertWebApiValues(api, expected)
    }
  }

  "generate partial json" should "succeed " in {
    val eventualApi = AMFCompiler(basePath + "completeExample.json", platform, Some(OasJsonHint))
      .build()
      .map(t => WebApiMaker(AMFUnit(t._1, basePath + "completeExample.json", Document, t._2)).make)

    val expected = List(
      ("name", "test"),
      ("description", "testDescription"),
      ("host", "api.example.com"),
      ("scheme", List("http", "https")),
      ("basePath", "http://api.example.com/path"),
      ("contentType", "application/json"),
      ("accepts", "application/json"),
      ("version", "1.1"),
      ("termsOfService", "terminos"),
      ("provider", new Organization("urlContact", "nameContact", "emailContact")),
      ("license", new License("urlLicense", "nameLicense")),
      ("documentation", new CreativeWork("urlExternalDocs", "descriptionExternalDocs"))
    )

    eventualApi map { api =>
      assertWebApiValues(api, expected)
    }
  }

  "edit complete " should "succeed " in {
    val eventualApi = AMFCompiler(basePath + "completeExample.raml", platform, Some(RamlYamlHint))
      .build()
      .map(t => WebApiMaker(AMFUnit(t._1, basePath + "completeExample.raml", Document, t._2)).make)

    val expected = List(
      ("name", "test"),
      ("description", "testDescription"),
      ("host", "http://api.example.com/path"),
      ("scheme", List("http", "https")),
      ("basePath", "/path"),
      ("contentType", "application/yaml"),
      ("accepts", "application/yaml"),
      ("version", "1.1"),
      ("termsOfService", "terminos"),
      ("provider", new Organization("urlContacto", "nombreContacto", "mailContacto")),
      ("license", new License("urlLicense", "nameLicense")),
      ("documentation", new CreativeWork("urlExternalDocs", "descriptionExternalDocs"))
    )

    val expectedAfter = List(
      ("name", "test"),
      ("description", "changed"),
      ("host", "http://api.example.com/path"),
      ("scheme", List("http", "https")),
      ("basePath", "/path"),
      ("contentType", "application/yaml"),
      ("accepts", "application/yaml"),
      ("version", "1.1"),
      ("termsOfService", "terminos"),
      ("provider", new Organization("urlContacto", "nombreContacto", "mailContacto")),
      ("license", new License("urlLicense", "changed")),
      ("documentation", new CreativeWork("urlExternalDocs", "descriptionExternalDocs"))
    )

    eventualApi map { api =>
      assertWebApiValues(api, expected)

      val builder = api.toBuilder
      builder.withDescription("changed")
      val licenseBuilder = builder.license.toBuilder
      licenseBuilder.withName("changed")
      builder.withLicense(licenseBuilder.build)
      val newWebApi = builder.build

      assertWebApiValues(newWebApi, expectedAfter)
    }
  }

  "edit complete json" should "succeed " in {
    val eventualApi = AMFCompiler(basePath + "completeExample.json", platform, Some(OasJsonHint))
      .build()
      .map(t => WebApiMaker(AMFUnit(t._1, basePath + "completeExample.json", Document, t._2)).make)

    val expected = List(
      ("name", "test"),
      ("description", "testDescription"),
      ("host", "api.example.com"),
      ("scheme", List("http", "https")),
      ("basePath", "http://api.example.com/path"),
      ("contentType", "application/json"),
      ("accepts", "application/json"),
      ("version", "1.1"),
      ("termsOfService", "terminos"),
      ("provider", new Organization("urlContact", "nameContact", "emailContact")),
      ("license", new License("urlLicense", "nameLicense")),
      ("documentation", new CreativeWork("urlExternalDocs", "descriptionExternalDocs"))
    )

    eventualApi map { api =>
      assertWebApiValues(api, expected)
    }

    val expectedAfter = List(
      ("name", "test"),
      ("description", "changed"),
      ("host", "api.example.com"),
      ("scheme", List("http", "https")),
      ("basePath", "http://api.example.com/path"),
      ("contentType", "application/json"),
      ("accepts", "application/json"),
      ("version", "1.1"),
      ("termsOfService", "terminos"),
      ("provider", new Organization("urlContact", "nameContact", "changed")),
      ("license", new License("urlLicense", "nameLicense")),
      ("documentation", new CreativeWork("urlExternalDocs", "descriptionExternalDocs"))
    )

    eventualApi map { api =>
      assertWebApiValues(api, expected)

      val builder = api.toBuilder
      builder.withDescription("changed")
      val providerBuilder = builder.provider.toBuilder
      providerBuilder.withEmail("changed")
      builder.withProvider(providerBuilder.build)
      val newWebApi = builder.build

      assertWebApiValues(newWebApi, expectedAfter)
    }
  }

  def assertWebApiValues(webApi: WebApi, assertions: List[(String, Any)]): Assertion = {
    assertions
      .map(t =>
        t._1 match {
          case "name" if t._2 != webApi.name => throwFail("name", t._2.toString, webApi.name)
          case "description" if t._2 != webApi.description =>
            throwFail("description", t._2.toString, webApi.description)
          case "host" if t._2 != webApi.host => throwFail("host", t._2.toString, webApi.host)
          case "scheme" =>
            assertWebApiSchemes(webApi.scheme, t._2.asInstanceOf[List[String]]) //throwFail("scheme",t._2.toString,webApi.name)
          case "basePath" if t._2 != webApi.basePath => throwFail("basePath", t._2.toString, webApi.basePath)
          case "host" if t._2 != webApi.host         => throwFail("host", t._2.toString, webApi.host)
          case "accepts" if t._2 != webApi.accepts   => throwFail("accepts", t._2.toString, webApi.accepts)
          case "contentType" if t._2 != webApi.contentType =>
            throwFail("contentType", t._2.toString, webApi.contentType)
          case "version" if t._2 != webApi.version => throwFail("version", t._2.toString, webApi.version)
          case "termsOfService" if t._2 != webApi.termsOfService =>
            throwFail("termsOfService", t._2.toString, webApi.termsOfService)
          case "provider"      => assertProvider(webApi.provider, t._2.asInstanceOf[Organization])
          case "license"       => assertLicence(webApi.license, t._2.asInstanceOf[License])
          case "documentation" => assertDocumentation(webApi.documentation, t._2.asInstanceOf[CreativeWork])
          case _               => succeed
      })
      .count(a => a == succeed) should be(assertions.size)
  }

  def assertWebApiSchemes(schemes: List[String], expected: List[String]): Assertion = {
    assert(schemes, expected)

  }

  def assertProvider(provider: Organization, expected: Organization): Assertion = {
    provider.email should be(expected.email)
    provider.url should be(expected.url)
    provider.name should be(expected.name)
  }

  def assertLicence(license: License, expected: License): Assertion = {
    if (expected == null) {
      license.url should be(null)
      license.name should be(null)
    } else {
      license.url should be(expected.url)
      license.name should be(expected.name)
    }
  }

  def assertDocumentation(documentation: CreativeWork, expected: CreativeWork): Assertion = {
    documentation.url should be(expected.url)
    documentation.description should be(expected.description)
  }

  def throwFail(field: String, expected: String, actual: String): Assertion = {
    fail(s"Field $field expected: $expected but actual: $actual")
  }
}
