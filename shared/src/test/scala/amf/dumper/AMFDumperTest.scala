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

    new AMFDumper(doc(api()), Oas).dump should be(expected)
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

    new AMFDumper(doc(api()), Raml).dump should be(expected)
  }

  ignore("Test simple amf/jsonld") {
    val expected =
      """{
        |  "http://raml.org/vocabularies/document#encodes": [
        |    {
        |      "http://schema.org/name": [
        |        {
        |          "@value": "test"
        |        }
        |      ],
        |      "http://raml.org/vocabularies/http#host": [
        |        {
        |          "@value": "http://localhost.com/api"
        |        }
        |      ],
        |      "http://raml.org/vocabularies/http#scheme": [
        |        {
        |          "@value": "http"
        |        },
        |        {
        |          "@value": "https"
        |        }
        |      ]
        |    }
        |  ]
        |}""".stripMargin

    new AMFDumper(doc(api()), Amf).dump should be(expected)
  }
}
