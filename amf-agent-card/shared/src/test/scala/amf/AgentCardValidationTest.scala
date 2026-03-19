package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class AgentCardValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = AgentCardTestConfig.config
}
