package amf.jsonschema

import amf.apicontract.client.scala.{AMFConfiguration, ConfigurationAdapter}
import amf.shapes.client.scala.config.JsonSchemaConfiguration
import amf.validation.UniquePlatformReportGenTest

class JsonSchemaValidationTest extends UniquePlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/jsonschema/schemas/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/jsonschema/reports/"
  val config: AMFConfiguration     = ConfigurationAdapter.adapt(JsonSchemaConfiguration.JsonSchema())

  test("JSON Schema without $schema key") {
    validate(
      "multiple-declarations-key.json",
      Some("multiple-declarations-key.json.report"),
      configOverride = Some(config)
    )
  }

}
