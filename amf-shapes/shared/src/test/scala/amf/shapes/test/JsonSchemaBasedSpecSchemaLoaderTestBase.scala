package amf.shapes.test

import amf.core.client.common.validation.SeverityLevels
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import org.scalatest.matchers.should.Matchers

abstract class JsonSchemaBasedSpecSchemaLoaderTestBase
    extends AsyncFunSuiteWithPlatformGlobalExecutionContext
    with Matchers {

  def testConfig: JsonSchemaBasedSpecTestConfig

  test(s"Validate that ${testConfig.specName} Schema has no errors") {
    testConfig.schemaLoader.doc != null shouldBe true
    testConfig.schemaLoader.schema != null shouldBe true
    testConfig.schemaShapeCheck(testConfig.schemaLoader.schema) shouldBe true
    filterErrors(testConfig.schemaLoader.errors).size shouldBe 0
  }

  // Adding this to ignore the "possibly-ignored-pattern-warning" that is not a real error and metadata errors
  private def filterErrors(errors: Seq[AMFValidationResult]): Seq[AMFValidationResult] = {
    errors
      .filterNot(_.severityLevel == SeverityLevels.VIOLATION)
      .filter(_.validationId == "http://a.ml/vocabularies/data#invalid-type-use")
  }
}
