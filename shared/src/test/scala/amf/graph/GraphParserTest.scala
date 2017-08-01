package amf.graph

import amf.document.Document
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
    """[{
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
      |}]""".stripMargin

  test("Document encoding simple WebApi") {
    testWebApiGraphParser(YeastASTBuilder(JsonLexer(`Document/WebApi/bare/jsonld/expanded`)))
  }

  private def testWebApiGraphParser(builder: YeastASTBuilder) = {
    val parser = new AmfParser(builder)

    val ast = builder.root()(parser.parse)

    val unit = GraphParser.parse(ast, "file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml")
    unit shouldBe a[Document]

    val document = unit.asInstanceOf[Document]
    document.location should be("file://shared/src/test/resources/tck/raml-1.0/Api/test003/api.raml")

    val api = document.encodes
    api.host should be("api.example.com")
    api.name should be("test")
  }
}
