package amf.dumper

import amf.broker.AMFUnitFixtureTest
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite
import org.scalatest.Matchers._

import scala.concurrent.ExecutionContext

/**
  * Created by hernan.najles on 7/10/17.
  */
class AMFUnitDumperTest extends AsyncFunSuite with PlatformSecrets with AMFUnitFixtureTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("test simple oas/json dump from given tree") {
    val webApi = buildWebApiClass()

    val dumper = new AMFDumper(webApi, Oas)

    val expected =
      """{
        |  "consumes": "application/json",
        |  "produces": "application/json",
        |  "schemes": [
        |    "http",
        |    "https"
        |  ],
        |  "basePath": "http://localhost.com/api",
        |  "host": "http://localhost.com/api",
        |  "info": {
        |    "title": "test",
        |    "description": "test description",
        |    "version": "1.1",
        |    "termsOfService": "termsOfService"
        |  }
        |}""".stripMargin
    dumper.dump() map { s =>
      s should be(expected)
    }
  }

  test("test simple raml/yaml dump from given tree") {

    val webApi = buildWebApiClass()
    val dumper = new AMFDumper(webApi, Raml)

    val expected =
      """title: test
        |description: test description
        |mediaType: application/json
        |version: 1.1
        |termsOfService: termsOfService
        |mediaType: application/json
        |protocols:
        |  - http
        |  - https
        |baseUri: http://localhost.com/api""".stripMargin
    dumper.dump() map { s =>
      s should be(expected)
    }
  }

  test("test simple amf/jsonld dump from given tree") {

    val webApi = buildWebApiClass()

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
