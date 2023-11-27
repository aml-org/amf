package amf.jsonschema

import amf.core.client.scala.model.document.Document
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.shapes.client.scala.config.JsonSchemaConfiguration.JsonSchema
import amf.shapes.client.scala.model.domain.{AnyShape, NodeShape}
import org.scalatest.matchers.should.Matchers

class JsonSchemaDocumentModelTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  private val base = "file://amf-cli/shared/src/test/resources/jsonschema/doc/"

  test("Link in JsonSchema doc should have link") {
    val client = JsonSchema().baseUnitClient()
    for {
      parsed <- client.parse(base + "simple.json")
    } yield {
      val shape     = parsed.baseUnit.asInstanceOf[Document].encodes.asInstanceOf[NodeShape]
      val stateProp = shape.properties.find(p => p.name.option().contains("state")).get
      stateProp.range.isLink shouldBe true
      stateProp.range.linkTarget.get shouldBe a[AnyShape]
    }
  }
}
