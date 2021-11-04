package amf.semanticjsonschema

import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class JsonLdSchemaSyntaxTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext = ExecutionContext.Implicits.global

  test("Test: shouldn't throw error if content passed is not a map") {
    val client = SemanticJsonSchemaConfiguration.predefined().baseUnitClient()
    client.parseContent("non-map content").map { result =>
      result.conforms shouldBe false
      result.results should have length 1
      result.results.head.validationId should include("syaml")
    }
  }
}
