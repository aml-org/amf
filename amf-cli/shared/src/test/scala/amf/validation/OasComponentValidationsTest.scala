package amf.validation

import amf.apicontract.client.scala.OASConfiguration

class OasComponentValidationsTest extends UniquePlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/components/oas3/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/components/oas3/reports/"

  private val componentConfig = OASConfiguration.OAS30Component()
  private val oas3Config      = OASConfiguration.OAS30()

  test("Minimal valid oas30 component") {
    validate("minimal-valid.yaml")
  }

  test("Valid components example") {
    validate("simple-components.yaml", configOverride = Some(componentConfig))
  }

  test("Valid components for Oas30") {
    validate("simple-components.yaml", configOverride = Some(oas3Config))
  }

  test("'paths' is mandatory") {
    validate("invalid-no-paths.yaml", Some("invalid-no-paths.report"), configOverride = Some(componentConfig))
  }

  test("'title' is mandatory") {
    validate("invalid-no-title.yaml", Some("invalid-no-title.report"), configOverride = Some(componentConfig))
  }

  test("'version' is mandatory") {
    validate("invalid-no-version.yaml", Some("invalid-no-version.report"), configOverride = Some(componentConfig))
  }

  test("'paths' must be an empty object") {
    validate(
      "invalid-non-empty-paths.yaml",
      Some("invalid-non-empty-paths.report"),
      configOverride = Some(componentConfig)
    )
  }

  test("oas30 root extra keys are invalid in components doc") {
    validate(
      "invalid-oas30-root-keys-in-root.yaml",
      Some("invalid-oas30-root-keys-in-root.report"),
      configOverride = Some(componentConfig)
    )
  }

  test("oas30 info extra keys are invalid in components doc") {
    validate(
      "invalid-oas30-info-keys-in-info.yaml",
      Some("invalid-oas30-info-keys-in-info.report"),
      configOverride = Some(componentConfig)
    )
  }
}
