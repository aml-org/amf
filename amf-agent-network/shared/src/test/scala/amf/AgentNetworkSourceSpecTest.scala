package amf

import amf.shapes.test.JsonSchemaBasedSpecSourceSpecTestBase

class AgentNetworkSourceSpecTest extends JsonSchemaBasedSpecSourceSpecTestBase {
  override def testConfig = AgentNetworkTestConfig.config
}
