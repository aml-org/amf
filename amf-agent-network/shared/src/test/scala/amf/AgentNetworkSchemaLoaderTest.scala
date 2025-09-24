package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentnetwork.internal.plugins.parse.schema.AgentNetworkSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class AgentNetworkSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that AgentNetwork Schema has no errors") {
    AgentNetworkSchemaLoader.doc != null shouldBe true
    AgentNetworkSchemaLoader.schema != null shouldBe true
    AgentNetworkSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    AgentNetworkSchemaLoader.errors.size shouldBe 0
  }
}
