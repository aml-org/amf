package amf.semanticjsonschema

import amf.core.client.scala.config.ParsingOptions
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class JsonSchemaDialectValidationTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val config: SemanticJsonSchemaConfiguration      = SemanticJsonSchemaConfiguration.predefined()
  private val basePath: String = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/validation/"

  test("Simple allOf schema should pass with maxJSONComplexity option") {
    val client = config.withParsingOptions(ParsingOptions.apply(maxJSONComplexity = Some(10))).baseUnitClient()
    client.parse(basePath + "schema-with-simple-allOf.json").map { result =>
      result.conforms shouldBe true
    }
  }

  test("Simple allOf schema should fail with maxJSONComplexity option") {
    val client = config.withParsingOptions(ParsingOptions.apply(maxJSONComplexity = Some(1))).baseUnitClient()
    client.parse(basePath + "schema-with-simple-allOf.json").map { result =>
      result.conforms shouldBe false
      result.results.size shouldBe 1
      result.results.head.message shouldBe "The JSON Schema is too complex: it has a combining complexity of 9 when the maximum is 1"
    }
  }

  test("Complex allOf schema should fail with maxJSONComplexity option") {
    val client = config.withParsingOptions(ParsingOptions.apply(maxJSONComplexity = Some(15))).baseUnitClient()
    client.parse(basePath + "schema-with-complex-allOf.json").map { result =>
      result.conforms shouldBe false
      result.results.size shouldBe 1
      result.results.head.message shouldBe "The JSON Schema is too complex: it has a combining complexity of 20 when the maximum is 15"
    }
  }

  test("Multiple allOf schema should fail with maxJSONComplexity option") {
    val client = config.withParsingOptions(ParsingOptions.apply(maxJSONComplexity = Some(15))).baseUnitClient()
    client.parse(basePath + "schema-with-multiple-allOf.json").map { result =>
      result.conforms shouldBe false
      result.results.size shouldBe 1
      result.results.head.message shouldBe "The JSON Schema is too complex: it has a combining complexity of 40 when the maximum is 15"
    }
  }

}
