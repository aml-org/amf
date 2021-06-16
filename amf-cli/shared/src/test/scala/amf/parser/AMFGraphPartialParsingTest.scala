package amf.parser

import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.{AmfObject, DomainElement}
import amf.core.client.scala.parse.AMFParser
import amf.shapes.client.scala.config.ShapesConfiguration
import amf.shapes.client.scala.model.domain.ScalarShape
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class AMFGraphPartialParsingTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext = ExecutionContext.Implicits.global
  val basePath: String                   = "file://amf-cli/shared/src/test/resources/graphs/"

  test("test read declared shape from api") {
    parse(
      "api.source.jsonld",
      "amf://id#1",
      (obj: AmfObject) => {
        obj.isInstanceOf[ScalarShape] should be(true)
        val shape = obj.asInstanceOf[ScalarShape]
        shape.name.value() should be("birthday")
        succeed
      }
    )
  }

  test("test read root unit") {
    parse(
      "api.source.jsonld",
      "amf://id",
      (obj: AmfObject) => {
        obj.isInstanceOf[Document] should be(true)
        val doc = obj.asInstanceOf[Document]
        // encodes will not be parsed as is a web api, and the model builder is not plugged.
        doc.declares.head.isInstanceOf[ScalarShape] should be(true)
        succeed
      }
    )
  }

  test("test read non existing id") {
    parse("api.source.jsonld", "amf://id#50", (obj: AmfObject) => {
      val dom = obj.asInstanceOf[DomainElement]
      dom.id should be("amf://error-domain-element")
    })
  }

  def parse(path: String, startingPoint: String, assertionFn: AmfObject => Assertion): Future[Assertion] = {
    val configuration = ShapesConfiguration.predefined()
    for {
      content <- AMFParser.parseStartingPoint(basePath + path, startingPoint, configuration)
    } yield {
      assertionFn(content.element)
    }
  }

}
