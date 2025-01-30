package amf.semanticjsonschema

import amf.aml.client.scala.AMLDialectResult
import amf.core.internal.validation.core.ValidationSpecification
import amf.core.io.FileAssertionTest
import amf.shapes.client.scala.config.SemanticJsonSchemaConfiguration
import amf.shapes.internal.spec.jsonschema.semanticjsonschema.SemanticJsonSchemaValidations.UnsupportedConstraint
import org.scalatest.Assertion
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class InvalidJsonSchemaToDialectTransformations extends FileAssertionTest with Matchers {

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
