package amf.jsonschema

import amf.apicontract.client.scala.OASConfiguration
import amf.validation.UniquePlatformReportGenTest

class LegacyJsonSchemaTest extends UniquePlatformReportGenTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/jsonschema/legacy/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/jsonschema/legacy/"

  private val config = OASConfiguration.OAS30

  test("JsonSchema should be able to $ref to id without baseUri") {
    validate("id-reference/api.yaml", configOverride = Some(config))
  }
}
