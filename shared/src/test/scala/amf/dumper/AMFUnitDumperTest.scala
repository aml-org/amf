package amf.dumper

import amf.emit.AMFUnitFixtureTest
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  * AMFUnitDumperTest
  */
class AMFUnitDumperTest extends AsyncFunSuite with PlatformSecrets with AMFUnitFixtureTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test simple oas/json dump from given tree") {
    val webApi = api()

    val amfDumper = new AMFDumper(webApi, Oas)

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
    amfDumper.dump() map { s =>
      s should be(expected)
    }
  }

  test("test simple raml/yaml dump from given tree") {

    val webApi = api()
    val dumper = new AMFDumper(webApi, Raml)

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
    dumper.dump() map { s =>
      s should be(expected)
    }
  }

  ignore("test simple amf/jsonld dump from given tree") {

    val webApi = api()

    val dumper = new AMFDumper(webApi, Amf)

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
    dumper.dump() map { s =>
      s should be(expected)
    }
  }
}
