package amf

import amf.core.common.AsyncFunSuiteWithPlatformGlobalExecutionContext
import amf.agentdomain.internal.plugins.parse.schema.AgentDomainSchemaLoader
import amf.shapes.client.scala.model.domain.NodeShape
import org.scalatest.matchers.should.Matchers

class AgentDomainSchemaLoaderTest extends AsyncFunSuiteWithPlatformGlobalExecutionContext with Matchers {

  test("Validate that AgentDomain Schema has no errors") {
    AgentDomainSchemaLoader.doc != null shouldBe true
    AgentDomainSchemaLoader.schema != null shouldBe true
    AgentDomainSchemaLoader.schema.isInstanceOf[NodeShape] shouldBe true
    AgentDomainSchemaLoader.errors.size shouldBe 0
  }
}
