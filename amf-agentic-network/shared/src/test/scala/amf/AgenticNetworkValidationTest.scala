package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class AgenticNetworkValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = AgenticNetworkTestConfig.config
}
