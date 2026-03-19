package amf

import amf.shapes.test.JsonSchemaBasedSpecValidationTestBase

class OtherCardValidationTest extends JsonSchemaBasedSpecValidationTestBase {
  override def testConfig = OtherCardTestConfig.config
}
