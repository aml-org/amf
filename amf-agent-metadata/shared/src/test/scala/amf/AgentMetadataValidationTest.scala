package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class AgentMetadataValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = AgentMetadataTestConfig.config
}
