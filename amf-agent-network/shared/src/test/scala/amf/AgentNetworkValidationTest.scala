package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class AgentNetworkValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = AgentNetworkTestConfig.config
}
