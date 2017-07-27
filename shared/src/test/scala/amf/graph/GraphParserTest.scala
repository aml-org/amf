package amf.graph

import amf.document.Document
import amf.domain.WebApi
import amf.json.JsonLexer
import amf.parser.YeastASTBuilder
import amf.serialization.AmfParser
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * [[GraphParser]] test
  */
class GraphParserTest extends FunSuite {

  private val `Document/WebApi/bare/jsonld/expanded`: String =
    """{
      |    "@id": "file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml",
      |    "@type": [
      |        "http://raml.org/vocabularies/document#Document",
      |        "http://raml.org/vocabularies/document#Fragment",
      |        "http://raml.org/vocabularies/document#Module",
      |        "http://raml.org/vocabularies/document#Unit"
      |    ],
      |    "http://raml.org/vocabularies/document#encodes": [
      |        {
      |            "@id": "file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml#/api-documentation",
      |            "@type": [
      |                "http://schema.org/WebAPI",
      |                "http://raml.org/vocabularies/document#DomainElement"
      |            ],
      |            "http://raml.org/vocabularies/http#host": [
      |                {
      |                    "@value": "api.example.com"
      |                }
      |            ],
      |            "http://schema.org/name": [
      |                {
      |                    "@value": "test"
      |                }
      |            ]
      |        }
      |    ]
      |}""".stripMargin

  private val `Document/WebApi/bare/jsonld`: String =
    """{
      |    "@context": {
      |        "raml-doc": "http://raml.org/vocabularies/document#",
      |        "raml-http": "http://raml.org/vocabularies/http#",
      |        "schema-org": "http://schema.org/"
      |    },
      |    "@id": "file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml",
      |    "@type": [
      |        "raml-doc:Document",
      |        "raml-doc:Fragment",
      |        "raml-doc:Module",
      |        "raml-doc:Unit"
      |    ],
      |    "raml-doc:encodes": {
      |            "@id": "file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml#/api-documentation",
      |            "@type": [
      |                "schema-org:WebAPI",
      |                "raml-doc:DomainElement"
      |            ],
      |            "raml-http:host": "api.example.com",
      |            "schema-org:name": "test"
      |    }
      |}""".stripMargin

  private val `Document/WebApi/full/jsonld`: String =
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
      |  "@id": "file:///tmp/test",
      |  "@type": [
      |    "raml-doc:Document",
      |    "raml-doc:Fragment",
      |    "raml-doc:Module",
      |    "raml-doc:Unit"
      |  ],
      |  "raml-doc:location": "file:///tmp/test",
      |  "raml-doc:encodes": {
      |    "@id": "file:///tmp/test#/web-api",
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
      |    "schema-org:termsOfService": "termsOfService",
      |    "schema-org:provider": {
      |      "@id": "file:///tmp/test#/web-api/organization",
      |      "@type": [
      |        "schema-org:Organization",
      |        "raml-doc:DomainElement"
      |      ],
      |      "schema-org:email": "test@test",
      |      "schema-org:name": "organizationName",
      |      "schema-org:url": "organizationUrl"
      |    },
      |    "schema-org:license": {
      |      "@id": "file:///tmp/test#/web-api/license",
      |      "@type": [
      |        "raml-http:License",
      |        "raml-doc:DomainElement"
      |      ],
      |      "schema-org:name": "licenseName",
      |      "schema-org:url": "licenseUrl"
      |    },
      |    "schema-org:documentation": {
      |      "@id": "file:///tmp/test#/web-api/creative-work",
      |      "@type": [
      |        "schema-org:CreativeWork",
      |        "raml-doc:DomainElement"
      |      ],
      |      "schema-org:url": "creativoWorkUrl",
      |      "schema-org:description": "creativeWorkDescription"
      |    },
      |    "raml-http:endpoint": [
      |      {
      |        "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint",
      |        "@type": [
      |          "raml-http:EndPoint",
      |          "raml-doc:DomainElement"
      |        ],
      |        "schema-org:description": "test endpoint",
      |        "schema-org:name": "endpoint",
      |        "raml-http:path": "/endpoint",
      |        "hydra:supportedOperation": [
      |          {
      |            "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/get",
      |            "@type": [
      |              "hydra:Operation",
      |              "raml-doc:DomainElement"
      |            ],
      |            "schema-org:description": "test operation get",
      |            "schema-org:documentation": {
      |              "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/get/creative-work",
      |              "@type": [
      |                "schema-org:CreativeWork",
      |                "raml-doc:DomainElement"
      |              ],
      |              "schema-org:description": "documentation operation",
      |              "schema-org:url": "localhost:8080/endpoint/operation"
      |            },
      |            "hydra:method": "get",
      |            "schema-org:name": "test get",
      |            "raml-http:scheme": [
      |              "http"
      |            ],
      |            "raml-http:guiSummary": "summary of operation get"
      |          },
      |          {
      |            "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/post",
      |            "@type": [
      |              "hydra:Operation",
      |              "raml-doc:DomainElement"
      |            ],
      |            "schema-org:description": "test operation post",
      |            "schema-org:documentation": {
      |              "@id": "file:///tmp/test#/web-api/end-points/%2Fendpoint/post/creative-work",
      |              "@type": [
      |                "schema-org:CreativeWork",
      |                "raml-doc:DomainElement"
      |              ],
      |              "schema-org:description": "documentation operation",
      |              "schema-org:url": "localhost:8080/endpoint/operation"
      |            },
      |            "hydra:method": "post",
      |            "schema-org:name": "test post",
      |            "raml-http:scheme": [
      |              "http"
      |            ],
      |            "raml-http:guiSummary": "summary of operation post"
      |          }
      |        ]
      |      }
      |    ]
      |  }
      |}
    """.stripMargin

  test("Document encoding simple WebApi") {
    testWebApiGraphParser(YeastASTBuilder(JsonLexer(`Document/WebApi/bare/jsonld`)))
  }

  test("Document encoding simple WebApi (expanded)") {
    testWebApiGraphParser(YeastASTBuilder(JsonLexer(`Document/WebApi/bare/jsonld/expanded`)))
  }

  test("Document encoding full WebApi") {
    val builder = YeastASTBuilder(JsonLexer(`Document/WebApi/full/jsonld`))
    val parser  = new AmfParser(builder)

    val ast = builder.root()(parser.parse)

    val unit = GraphParser.parse(ast)
    unit shouldBe a[Document]

    val api = unit.asInstanceOf[Document].encodes

    api.endPoints should have size 1

    val endPoint = api.endPoints.head
    endPoint.operations should have size 2

    endPoint.operations.head.documentation.description should be("documentation operation")
  }

  private def testWebApiGraphParser(builder: YeastASTBuilder) = {
    val parser = new AmfParser(builder)

    val ast = builder.root()(parser.parse)

    val unit = GraphParser.parse(ast)
    unit shouldBe a[Document]

    val api = unit.asInstanceOf[Document].encodes
    api.host should be("api.example.com")
    api.name should be("test")
  }
}
