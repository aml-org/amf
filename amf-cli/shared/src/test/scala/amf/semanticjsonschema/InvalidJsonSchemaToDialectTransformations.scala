package amf.semanticjsonschema

import amf.aml.client.scala.AMLDialectResult
import amf.core.internal.remote.Hint
import amf.core.internal.unsafe.PlatformSecrets
import amf.core.internal.validation.core.ValidationSpecification
import amf.io.FileAssertionTest
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations.UnsupportedConstraint
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class InvalidJsonSchemaToDialectTransformations
    extends AsyncFunSuite
    with PlatformSecrets
    with FileAssertionTest
    with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val jsonSchemaPath = "file://amf-cli/shared/src/test/resources/semantic-jsonschema/json-schemas/"

  test("Json Schema with OR constraint should throw violation") {
    validate("anyOf.json", UnsupportedConstraint)
  }

  test("Json Schema with NOT constraint should throw violation") {
    validate("not.json", UnsupportedConstraint)
  }

  private def validate(schema: String, expectedError: ValidationSpecification): Future[Assertion] = {
    for {
      result <- parseSchema(jsonSchemaPath + schema)
    } yield {
      result.results should have length 1
      result.results.exists(p => p.validationId == expectedError.id) shouldBe true
    }
  }

  private def parseSchema(path: String): Future[AMLDialectResult] = {
    val config = SemanticJsonSchemaConfiguration.predefined()
    config.baseUnitClient().parseDialect(path)
  }
}
