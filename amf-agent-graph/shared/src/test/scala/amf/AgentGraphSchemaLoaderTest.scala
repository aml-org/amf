package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentgraph.internal.plugins.parse.schema.AgentGraphSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class AgentGraphSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that AgentGraph Schema has no errors") {
    AgentGraphSchemaLoader.doc != null shouldBe true
    AgentGraphSchemaLoader.schema != null shouldBe true
    AgentGraphSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    AgentGraphSchemaLoader.errors.size shouldBe 0
  }
}
