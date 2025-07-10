package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentfabric.internal.plugins.parse.schema.AgentFabricSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class AgentFabricSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that AgentFabric Schema has no errors") {
    AgentFabricSchemaLoader.doc != null shouldBe true
    AgentFabricSchemaLoader.schema != null shouldBe true
    AgentFabricSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    AgentFabricSchemaLoader.errors.size shouldBe 0
  }
}
