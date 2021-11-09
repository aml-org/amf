package amf.semanticjsonschema

import amf.core.client.scala.exception.UnsupportedDomainForDocumentException
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class JsonSchemaDialectSyntaxTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext = ExecutionContext.Implicits.global
  private val client                     = SemanticJsonSchemaConfiguration.predefined().baseUnitClient()
  private val basePath                   = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/syntax-error/"

  test("Throws exception if schema isn't a map that doesn't have $schema") {
    recoverToSucceededIf[UnsupportedDomainForDocumentException] {
      client.parseContent("non-map content")
    }
  }

  test("Violation when @context points to nowhere") {
    client.parse(basePath + "invalid-context-pointing-to-nowhere.json").map { result =>
      result.conforms shouldBe false
      result.results should have length 1
    }
  }

  test("Violation when @context is an array") {
    client.parse(basePath + "invalid-array-context.json").map { result =>
      result.conforms shouldBe false
      result.results should have length 1
    }
  }
}
