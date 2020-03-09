package amf.dumper

import amf.common.Tests
import amf.core.emitter.RenderOptions
import amf.core.remote.{Amf, Oas, Raml}
import amf.core.unsafe.PlatformSecrets
import amf.emit.{AMFRenderer, AMFUnitFixtureTest}
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

    new AMFRenderer(`document/api/bare`, Oas, RenderOptions(), None).renderToString.map(assert(_, expected))
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

    new AMFRenderer(`document/api/bare`, Raml, RenderOptions(), None).renderToString.map(assert(_, expected))
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
        |          "http://a.ml/vocabularies/apiContract#WebAPI",
        |          "http://a.ml/vocabularies/document#RootDomainElement",
        |          "http://a.ml/vocabularies/document#DomainElement"
        |        ],
        |        "http://a.ml/vocabularies/core#name": [
        |          {
        |            "@value": "test"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#description": [
        |          {
        |            "@value": "test description"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#server": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/localhost.com%2Fapi",
        |            "@type": [
        |              "http://a.ml/vocabularies/apiContract#Server",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/core#urlTemplate": [
        |              {
        |                "@value": "localhost.com/api"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#accepts": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#contentType": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#scheme": [
        |          {
        |            "@value": "http"
        |          },
        |          {
        |            "@value": "https"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#version": [
        |          {
        |            "@value": "1.1"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#termsOfService": [
        |          {
        |            "@value": "termsOfService"
        |          }
        |        ]
        |      }
        |    ],
        |    "http://a.ml/vocabularies/document#version": [
        |      {
        |        "@value": "2.1.0"
        |      }
        |    ],
        |    "http://a.ml/vocabularies/document#root": [
        |      {
        |        "@value": true
        |      }
        |    ]
        |  }
        |]
        |""".stripMargin

    new AMFRenderer(`document/api/bare`, Amf, RenderOptions(), None).renderToString.map(assert(_, expected))
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
        |          "http://a.ml/vocabularies/apiContract#WebAPI",
        |          "http://a.ml/vocabularies/document#RootDomainElement",
        |          "http://a.ml/vocabularies/document#DomainElement"
        |        ],
        |        "http://a.ml/vocabularies/core#name": [
        |          {
        |            "@value": "test"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#description": [
        |          {
        |            "@value": "test description"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#server": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/localhost.com%2Fapi",
        |            "@type": [
        |              "http://a.ml/vocabularies/apiContract#Server",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/core#urlTemplate": [
        |              {
        |                "@value": "localhost.com/api"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#accepts": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#contentType": [
        |          {
        |            "@value": "application/json"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#scheme": [
        |          {
        |            "@value": "http"
        |          },
        |          {
        |            "@value": "https"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#version": [
        |          {
        |            "@value": "1.1"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#termsOfService": [
        |          {
        |            "@value": "termsOfService"
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#provider": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/organization",
        |            "@type": [
        |              "http://a.ml/vocabularies/core#Organization",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/core#url": [
        |              {
        |                "@id": "organizationUrl"
        |              }
        |            ],
        |            "http://a.ml/vocabularies/core#name": [
        |              {
        |                "@value": "organizationName"
        |              }
        |            ],
        |            "http://a.ml/vocabularies/core#email": [
        |              {
        |                "@value": "test@test"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#license": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/license",
        |            "@type": [
        |              "http://a.ml/vocabularies/core#License",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/core#url": [
        |              {
        |                "@id": "licenseUrl"
        |              }
        |            ],
        |            "http://a.ml/vocabularies/core#name": [
        |              {
        |                "@value": "licenseName"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/core#documentation": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/creative-work/creativoWorkUrl",
        |            "@type": [
        |              "http://a.ml/vocabularies/core#CreativeWork",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/core#url": [
        |              {
        |                "@id": "creativoWorkUrl"
        |              }
        |            ],
        |            "http://a.ml/vocabularies/core#description": [
        |              {
        |                "@value": "creativeWorkDescription"
        |              }
        |            ]
        |          }
        |        ],
        |        "http://a.ml/vocabularies/apiContract#endpoint": [
        |          {
        |            "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint",
        |            "@type": [
        |              "http://a.ml/vocabularies/apiContract#EndPoint",
        |              "http://a.ml/vocabularies/document#DomainElement"
        |            ],
        |            "http://a.ml/vocabularies/apiContract#path": [
        |              {
        |                "@value": "/endpoint"
        |              }
        |            ],
        |            "http://a.ml/vocabularies/core#name": [
        |              {
        |                "@value": "endpoint"
        |              }
        |            ],
        |            "http://a.ml/vocabularies/core#description": [
        |              {
        |                "@value": "test endpoint"
        |              }
        |            ],
        |            "http://a.ml/vocabularies/apiContract#supportedOperation": [
        |              {
        |                "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/get",
        |                "@type": [
        |                  "http://a.ml/vocabularies/apiContract#Operation",
        |                  "http://a.ml/vocabularies/document#DomainElement"
        |                ],
        |                "http://a.ml/vocabularies/apiContract#method": [
        |                  {
        |                    "@value": "get"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/core#name": [
        |                  {
        |                    "@value": "test get"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/core#description": [
        |                  {
        |                    "@value": "test operation get"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/apiContract#guiSummary": [
        |                  {
        |                    "@value": "summary of operation get"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/core#documentation": [
        |                  {
        |                    "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/get/creative-work/localhost%3A8080%2Fendpoint%2Foperation",
        |                    "@type": [
        |                      "http://a.ml/vocabularies/core#CreativeWork",
        |                      "http://a.ml/vocabularies/document#DomainElement"
        |                    ],
        |                    "http://a.ml/vocabularies/core#url": [
        |                      {
        |                        "@id": "localhost:8080/endpoint/operation"
        |                      }
        |                    ],
        |                    "http://a.ml/vocabularies/core#description": [
        |                      {
        |                        "@value": "documentation operation"
        |                      }
        |                    ]
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/apiContract#scheme": [
        |                  {
        |                    "@value": "http"
        |                  }
        |                ]
        |              },
        |              {
        |                "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/post",
        |                "@type": [
        |                  "http://a.ml/vocabularies/apiContract#Operation",
        |                  "http://a.ml/vocabularies/document#DomainElement"
        |                ],
        |                "http://a.ml/vocabularies/apiContract#method": [
        |                  {
        |                    "@value": "post"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/core#name": [
        |                  {
        |                    "@value": "test post"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/core#description": [
        |                  {
        |                    "@value": "test operation post"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/core#deprecated": [
        |                  {
        |                    "@value": true
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/apiContract#guiSummary": [
        |                  {
        |                    "@value": "summary of operation post"
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/core#documentation": [
        |                  {
        |                    "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/post/creative-work/localhost%3A8080%2Fendpoint%2Foperation",
        |                    "@type": [
        |                      "http://a.ml/vocabularies/core#CreativeWork",
        |                      "http://a.ml/vocabularies/document#DomainElement"
        |                    ],
        |                    "http://a.ml/vocabularies/core#url": [
        |                      {
        |                        "@id": "localhost:8080/endpoint/operation"
        |                      }
        |                    ],
        |                    "http://a.ml/vocabularies/core#description": [
        |                      {
        |                        "@value": "documentation operation"
        |                      }
        |                    ]
        |                  }
        |                ],
        |                "http://a.ml/vocabularies/apiContract#scheme": [
        |                  {
        |                    "@value": "http"
        |                  }
        |                ]
        |              }
        |            ]
        |          }
        |        ]
        |      }
        |    ],
        |    "http://a.ml/vocabularies/document#version": [
        |      {
        |        "@value": "2.1.0"
        |      }
        |    ],
        |    "http://a.ml/vocabularies/document#root": [
        |      {
        |        "@value": true
        |      }
        |    ]
        |  }
        |]
        |""".stripMargin

    new AMFRenderer(`document/api/full`, Amf, RenderOptions(), None).renderToString.map(assert(_, expected))
  }

  test("Test string examples raml/yaml") {
    val expected =
      """#%RAML 1.0
        |title: test examples
        |/endpoint:
        | get:
        |   responses:
        |     "200":
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

    new AMFRenderer(`document/api/stringExamples`, Raml, RenderOptions(), None).renderToString.map(assert(_, expected))
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

    new AMFRenderer(`document/api/stringExamples`, Oas, RenderOptions(), None).renderToString.map(assert(_, expected))
  }

  private def assert(actual: String, expected: String): Assertion = {
    Tests.checkDiff(actual, expected)
    succeed
  }
}
