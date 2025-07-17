package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentcard.internal.plugins.parse.schema.AgentCardSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class AgentCardSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that AgentCard Schema has no errors") {
    AgentCardSchemaLoader.doc != null shouldBe true
    AgentCardSchemaLoader.schema != null shouldBe true
    AgentCardSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    AgentCardSchemaLoader.errors.size shouldBe 0
  }
}
