package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class AgentNetworkMetadataValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = AgentNetworkMetadataTestConfig.config
}
