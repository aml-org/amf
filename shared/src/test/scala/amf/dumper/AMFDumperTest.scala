package amf.dumper

import amf.emit.AMFUnitFixtureTest
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

/**
  * AMF Unit DumperTest
  */
class AMFDumperTest extends AsyncFunSuite with PlatformSecrets with AMFUnitFixtureTest {

  test("Test simple oas/json") {
    val expected =
      """{
        |  "swagger": "2.0",
        |  "info": {
        |    "title": "test",
        |    "description": "test description",
        |    "version": "1.1",
        |    "termsOfService": "termsOfService"
        |  },
        |  "host": "http://localhost.com/api",
        |  "schemes": [
        |    "http",
        |    "https"
        |  ],
        |  "basePath": "http://localhost.com/api",
        |  "consumes": "application/json",
        |  "produces": "application/json"
        |}""".stripMargin

    new AMFDumper(`document/api/bare`, Oas).dump should be(expected)
  }

  test("Test simple raml/yaml") {
    val expected =
      """#%RAML 1.0
        |title: test
        |description: test description
        |baseUri: http://localhost.com/api
        |protocols:
        |  - http
        |  - https
        |mediaType: application/json
        |version: 1.1
        |termsOfService: termsOfService""".stripMargin

    new AMFDumper(`document/api/bare`, Raml).dump should be(expected)
  }

  test("Test simple amf/jsonld") {
    val expected =
      """{
        |  "@context": {
        |    "raml-doc": "http://raml.org/vocabularies/document#",
        |    "raml-http": "http://raml.org/vocabularies/http#",
        |    "raml-shapes": "http://raml.org/vocabularies/shapes#",
        |    "hydra": "http://www.w3.org/ns/hydra/core#",
        |    "shacl": "http://www.w3.org/ns/shacl#",
        |    "schema-org": "http://schema.org/",
        |    "xsd": "http://www.w3.org/2001/XMLSchema#"
        |  },
        |  "@type": [
        |    "raml-doc:Document",
        |    "raml-doc:Fragment",
        |    "raml-doc:Module",
        |    "raml-doc:Unit"
        |  ],
        |  "raml-doc:encodes": {
        |    "@type": [
        |      "schema-org:WebAPI",
        |      "raml-doc:DomainElement"
        |    ],
        |    "schema-org:name": "test",
        |    "schema-org:description": "test description",
        |    "raml-http:host": "http://localhost.com/api",
        |    "raml-http:schemes": [
        |      "http",
        |      "https"
        |    ],
        |    "raml-http:basePath": "http://localhost.com/api",
        |    "raml-http:accepts": "application/json",
        |    "raml-http:contentType": "application/json",
        |    "schema-org:version": "1.1",
        |    "schema-org:termsOfService": "termsOfService"
        |  }
        |}""".stripMargin

    new AMFDumper(`document/api/bare`, Amf).dump should be(expected)
  }
}
