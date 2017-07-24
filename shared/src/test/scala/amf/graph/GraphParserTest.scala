package amf.graph

import amf.compiler.Root
import amf.document.Document
import amf.domain.WebApi
import amf.json.JsonLexer
import amf.parser.YeastASTBuilder
import amf.remote.Amf
import amf.serialization.AmfParser
import org.scalatest.FunSuite
import org.scalatest.Matchers._

/**
  * [[GraphParser]] test
  */
class GraphParserTest extends FunSuite {

  private val `Document/WebApi/jsonld/expanded`: String =
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

  private val `Document/WebApi/jsonld`: String =
    """{
      |    "@context": {
      |        "raml-doc": "http://raml.org/vocabularies/document#",
      |        "raml-http": "http://raml.org/vocabularies/http#",
      |        "schema-org": "http://schema.org/",
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

  test("Document encoding simple WebApi (using @context)") {
    testWebApiGraphParser(YeastASTBuilder(JsonLexer(`Document/WebApi/jsonld`)))
  }

  test("Document encoding simple WebApi (expanded)") {
    testWebApiGraphParser(YeastASTBuilder(JsonLexer(`Document/WebApi/jsonld/expanded`)))
  }

  private def testWebApiGraphParser(builder: YeastASTBuilder) = {
    val parser = new AmfParser(builder)

    val ast = builder.root() {
      parser.parse
    }

    val root =
      Root(ast, "file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml", Nil, Amf)

    val unit = GraphParser.parse(root)
    unit shouldBe a[Document]

    val document = unit.asInstanceOf[Document]
    document.encodes shouldBe a[WebApi]

    val api = document.encodes.asInstanceOf[WebApi]
    api.host should be("api.example.com")
    api.name should be("test")
  }
}
