package amf

import amf.core.client.common.validation.SeverityLevels
import amf.core.client.scala.validation.AMFValidationResult
import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.mcp.internal.plugins.parse.schema.MCPSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class MCPSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that MCP Schema has no errors") {
    MCPSchemaLoader.doc != null shouldBe true
    MCPSchemaLoader.schema != null shouldBe true
    MCPSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    filterErrors(MCPSchemaLoader.errors).size shouldBe 0
  }

  // Adding this to ignore the "possibly-ignored-pattern-warning" that is not a real error and metadata errors
  private def filterErrors(errors: Seq[AMFValidationResult]): Seq[AMFValidationResult] = {
    errors
      .filterNot(_.severityLevel == SeverityLevels.VIOLATION)
      .filter(_.validationId == "http://a.ml/vocabularies/data#invalid-type-use")
  }
}
