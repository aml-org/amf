package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentgraph.internal.plugins.parse.schema.AgentGraphSchemaLoader
import amf.core.client.common.validation.SeverityLevels
import amf.core.client.scala.validation.AMFValidationResult
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class AgentGraphSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that AgentGraph Schema has no errors") {
    AgentGraphSchemaLoader.doc != null shouldBe true
    AgentGraphSchemaLoader.schema != null shouldBe true
    AgentGraphSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    filterErrors(AgentGraphSchemaLoader.errors).size shouldBe 0
  }

  // Adding this to ignore the "possibly-ignored-pattern-warning" that is not a real error and metadata errors
  private def filterErrors(errors: Seq[AMFValidationResult]): Seq[AMFValidationResult] = {
    errors
      .filterNot(_.severityLevel == SeverityLevels.VIOLATION)
      .filter(_.validationId == "http://a.ml/vocabularies/data#invalid-type-use")
  }
}
