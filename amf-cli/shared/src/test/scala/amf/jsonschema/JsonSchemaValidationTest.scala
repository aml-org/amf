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

  test("JSON Schema with schema raw validation violation") {
    validate(
      "empty-enum.json",
      Some("empty-enum.json.report"),
      configOverride = Some(config)
    )
  }

  if (platform.name == "jvm") {
    test("JSON Schema with payload validation violation") {
      validate(
        "invalid-example.json",
        Some("invalid-example.json.report"),
        configOverride = Some(config)
      )
    }
  }

  test("JSON Schema with const property should be valid in Draft 7 or newer") {
    validate(
      "const.json",
      configOverride = Some(config)
    )
  }

  test("JSON Schema with conditionals should be valid in Draft 7 or newer") {
    validate(
      "conditionals.json",
      configOverride = Some(config)
    )
  }
}
