package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentsdomain.internal.plugins.parse.schema.AgentsDomainSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class AgentsDomainSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that AgentsDomain Schema has no errors") {
    AgentsDomainSchemaLoader.doc != null shouldBe true
    AgentsDomainSchemaLoader.schema != null shouldBe true
    AgentsDomainSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    AgentsDomainSchemaLoader.errors.size shouldBe 0
  }
}
