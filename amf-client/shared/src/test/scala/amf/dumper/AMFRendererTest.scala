package amf.dumper

import amf.core.emitter.RenderOptions
import amf.common.Tests
import amf.core.remote.Syntax.{Json, Yaml}
import amf.core.remote.{Amf, Oas, Raml}
import amf.core.unsafe.PlatformSecrets
import amf.emit.AMFUnitFixtureTest
import amf.facades.AMFRenderer
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.ExecutionContext

/**
  * AMF Unit DumperTest
  */
class AMFRendererTest extends AsyncFunSuite with PlatformSecrets with AMFUnitFixtureTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Test simple oas/json") {
    val expected =
      """{
        |  "swagger": "2.0",
        |  "info": {
        |    "title": "test",
        |    "description": "test description",
        |    "termsOfService": "termsOfService",
        |    "version": "1.1"
        |  },
        |  "host": "localhost.com",
        |  "basePath": "/api",
        |    "consumes": [
        |    "application/json"
        |  ],
        |    "produces": [
        |    "application/json"
        |  ],
        |  "schemes": [
        |    "http",
        |    "https"
        |  ],
        |  "paths": {}
        |}""".stripMargin

    new AMFRenderer(`document/api/bare`, Oas, Json, RenderOptions()).renderToString.map(assert(_, expected))
  }

  test("Test simple raml/yaml") {
    val expected =
      """#%RAML 1.0
        |title: test
        |baseUri: localhost.com/api
        |description: test description
        |mediaType:
        |  - application/json
        |version: "1.1"
        |(amf-termsOfService): termsOfService
        |protocols:
        |  - http
        |  - https""".stripMargin

    new AMFRenderer(`document/api/bare`, Raml, Yaml, RenderOptions()).renderToString.map(assert(_, expected))
  }

  test("Test simple amf/jsonld") {
    val expected =
      """[
        |  {
        |    "@id": "file:///tmp/test",
        |    "@type": [
        |      "http://a.ml/vocabularies/document#Document",
        |      "http://a.ml/vocabularies/document#Fragment",
        |      "http://a.ml/vocabularies/document#Module",
        |      "http://a.ml/vocabularies/document#Unit"
        |    ],
        |    "http://a.ml/vocabularies/document#encodes": [
        |      {
        |        "@id": "file:///tmp/test#/web-api",
        |        "@type": [
        |          "http://schema.org/WebAPI",
        |          "http://a.ml/vocabularies/document#RootDomainElement",
        |          "http://a.ml/vocabularies/document#DomainElement"
        |        ],
        |        "http://schema.org/name": [
        |          {
        |            "@value": "test"
        |          }
        |        ],
        |        "http://schema.org/description": [
        |          {
        |            "@value": "test description"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#server": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/localhost.com%2Fapi",
        |            "@type": [
        |              "http://a.ml/vocabularies/http#Server",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/http#url": [
        |              {
        |                "@value": "localhost.com/api"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#accepts": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#contentType": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#scheme": [
        |          {
        |            "@value": "http"
        |          },
        |          {
        |            "@value": "https"
        |          }
        |        ],
        |        "http://schema.org/version": [
        |          {
        |            "@value": "1.1"
        |          }
        |        ],
        |        "http://schema.org/termsOfService": [
        |          {
        |            "@value": "termsOfService"
        |          }
        |        ]
        |      }
        |    ]
        |  }
        |]""".stripMargin

    new AMFRenderer(`document/api/bare`, Amf, Json, RenderOptions()).renderToString.map(assert(_, expected))
  }

  test("Test full amf/jsonld") {
    val expected =
      """[
        |  {
        |    "@id": "file:///tmp/test",
        |    "@type": [
        |      "http://a.ml/vocabularies/document#Document",
        |      "http://a.ml/vocabularies/document#Fragment",
        |      "http://a.ml/vocabularies/document#Module",
        |      "http://a.ml/vocabularies/document#Unit"
        |    ],
        |    "http://a.ml/vocabularies/document#encodes": [
        |      {
        |        "@id": "file:///tmp/test#/web-api",
        |        "@type": [
        |          "http://schema.org/WebAPI",
        |          "http://a.ml/vocabularies/document#RootDomainElement",
        |          "http://a.ml/vocabularies/document#DomainElement"
        |        ],
        |        "http://schema.org/name": [
        |          {
        |            "@value": "test"
        |          }
        |        ],
        |        "http://schema.org/description": [
        |          {
        |            "@value": "test description"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#server": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/localhost.com%2Fapi",
        |            "@type": [
        |              "http://a.ml/vocabularies/http#Server",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/http#url": [
        |              {
        |                "@value": "localhost.com/api"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#accepts": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#contentType": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#scheme": [
        |          {
        |            "@value": "http"
        |          },
        |          {
        |            "@value": "https"
        |          }
        |        ],
        |        "http://schema.org/version": [
        |          {
        |            "@value": "1.1"
        |          }
        |        ],
        |        "http://schema.org/termsOfService": [
        |          {
        |            "@value": "termsOfService"
        |          }
        |        ],
        |        "http://schema.org/provider": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/organization",
        |            "@type": [
        |              "http://schema.org/Organization",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://schema.org/url": [
        |              {
        |                "@id": "organizationUrl"
        |              }
        |            ],
        |            "http://schema.org/name": [
        |              {
        |                "@value": "organizationName"
        |              }
        |            ],
        |            "http://schema.org/email": [
        |              {
        |                "@value": "test@test"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://schema.org/license": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/license",
        |            "@type": [
        |              "http://a.ml/vocabularies/http#License",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://schema.org/url": [
        |              {
        |                "@id": "licenseUrl"
        |              }
        |            ],
        |            "http://schema.org/name": [
        |              {
        |                "@value": "licenseName"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://schema.org/documentation": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/creative-work/creativoWorkUrl",
        |            "@type": [
        |              "http://schema.org/CreativeWork",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://schema.org/url": [
        |              {
        |                "@id": "creativoWorkUrl"
        |              }
        |            ],
        |            "http://schema.org/description": [
        |              {
        |                "@value": "creativeWorkDescription"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/http#endpoint": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint",
        |            "@type": [
        |              "http://a.ml/vocabularies/http#EndPoint",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/http#path": [
        |              {
        |                "@value": "/endpoint"
        |              }
        |            ],
        |            "http://schema.org/name": [
        |              {
        |                "@value": "endpoint"
        |              }
        |            ],
        |            "http://schema.org/description": [
        |              {
        |                "@value": "test endpoint"
        |              }
        |            ],
        |            "http://www.w3.org/ns/hydra/core#supportedOperation": [
        |              {
        |                "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/get",
        |                "@type": [
        |                  "http://www.w3.org/ns/hydra/core#Operation",
        |                  "http://a.ml/vocabularies/document#DomainElement"
        |                ],
        |                "http://www.w3.org/ns/hydra/core#method": [
        |                  {
        |                    "@value": "get"
        |                  }
        |                ],
        |                "http://schema.org/name": [
        |                  {
        |                    "@value": "test get"
        |                  }
        |                ],
        |                "http://schema.org/description": [
        |                  {
        |                    "@value": "test operation get"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/http#guiSummary": [
        |                  {
        |                    "@value": "summary of operation get"
        |                  }
        |                ],
        |                "http://schema.org/documentation": [
        |                  {
        |                    "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/get/creative-work/localhost%3A8080%2Fendpoint%2Foperation",
        |                    "@type": [
        |                      "http://schema.org/CreativeWork",
        |                      "http://a.ml/vocabularies/document#DomainElement"
        |                    ],
        |                    "http://schema.org/url": [
        |                      {
        |                        "@id": "localhost:8080/endpoint/operation"
        |                      }
        |                    ],
        |                    "http://schema.org/description": [
        |                      {
        |                        "@value": "documentation operation"
        |                      }
        |                    ]
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/http#scheme": [
        |                  {
        |                    "@value": "http"
        |                  }
        |                ]
        |              },
        |              {
        |                "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/post",
        |                "@type": [
        |                  "http://www.w3.org/ns/hydra/core#Operation",
        |                  "http://a.ml/vocabularies/document#DomainElement"
        |                ],
        |                "http://www.w3.org/ns/hydra/core#method": [
        |                  {
        |                    "@value": "post"
        |                  }
        |                ],
        |                "http://schema.org/name": [
        |                  {
        |                    "@value": "test post"
        |                  }
        |                ],
        |                "http://schema.org/description": [
        |                  {
        |                    "@value": "test operation post"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/document#deprecated": [
        |                  {
        |                    "@value": true
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/http#guiSummary": [
        |                  {
        |                    "@value": "summary of operation post"
        |                  }
        |                ],
        |                "http://schema.org/documentation": [
        |                  {
        |                    "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/post/creative-work/localhost%3A8080%2Fendpoint%2Foperation",
        |                    "@type": [
        |                      "http://schema.org/CreativeWork",
        |                      "http://a.ml/vocabularies/document#DomainElement"
        |                    ],
        |                    "http://schema.org/url": [
        |                      {
        |                        "@id": "localhost:8080/endpoint/operation"
        |                      }
        |                    ],
        |                    "http://schema.org/description": [
        |                      {
        |                        "@value": "documentation operation"
        |                      }
        |                    ]
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/http#scheme": [
        |                  {
        |                    "@value": "http"
        |                  }
        |                ]
        |              }
        |            ]
        |          }
        |        ]
        |      }
        |    ]
        |  }
        |]""".stripMargin

    new AMFRenderer(`document/api/full`, Amf, Json, RenderOptions()).renderToString.map(assert(_, expected))
  }

  test("Test string examples raml/yaml") {
    val expected =
      """#%RAML 1.0
        |title: test examples
        |/endpoint:
        | get:
        |   responses:
        |     200:
        |       body:
        |        application/json:
        |         example:
        |           name: roman
        |           lastName: riquelme
        |         properties:
        |           name:
        |             type: string
        |           lastName:
        |             type: string
        |       (amf-examples):
        |         application/json:
        |           name: Cristian
        |           lastName: Pavon""".stripMargin

    new AMFRenderer(`document/api/stringExamples`, Raml, Yaml, RenderOptions()).renderToString.map(assert(_, expected))
  }

  test("Test string examples oas/json") {
    val expected =
      """{
        |  "swagger": "2.0",
        |  "info": {
        |    "title": "test examples",
        |    "version": "1.0"
        |  },
        |  "paths": {
        |   "/endpoint": {
        |     "get": {
        |       "responses": {
        |         "200": {
        |           "description": "",
        |           "x-amf-mediaType": "application/json",
        |           "schema": {
        |              "example": {
        |                "name": "roman",
        |                "lastName": "riquelme"
        |              },
        |              "type": "object",
        |              "required": [
        |                "name",
        |                "lastName"
        |              ],
        |              "properties": {
        |                "name": {
        |                  "type": "string"
        |                },
        |                "lastName": {
        |                  "type": "string"
        |                }
        |              }
        |           },
        |           "examples": {
        |             "application/json": {
        |               "name": "Cristian",
        |               "lastName": "Pavon"
        |             }
        |           }
        |         }
        |       }
        |     }
        |   }
        |  }
        |}""".stripMargin

    new AMFRenderer(`document/api/stringExamples`, Oas, Json, RenderOptions()).renderToString.map(assert(_, expected))
  }

  private def assert(actual: String, expected: String): Assertion = {
    Tests.checkDiff(actual, expected)
    succeed
  }
}
