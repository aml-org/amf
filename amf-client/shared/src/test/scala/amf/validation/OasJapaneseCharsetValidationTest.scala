package amf.validation

import amf.core.remote.{Hint, OasYamlHint}

// TODO: Add test to check for Japanese characters in OAS 3 Component schema name. @author: Tomás Fernández
class OasJapaneseCharsetValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/japanese/oas/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/japanese/oas/"
  override val hint: Hint          = OasYamlHint

  test("Documentation server params") {
    validate("documentation_server_params.json")
  }

  test("Documentation path and response description") {
    validate("documentation_path_and_response_description.json")
  }

  test("Documentation path item description") {
    validate("documentation_path_item_description.json")
  }

  test("Documentation security schemes") {
    validate("documentation_security_schemes.json")
  }

  test("Facet string pattern valid") {
    validate("facet_string_pattern_valid.yaml")
  }

  test("Facet string pattern invalid") {
    validate("facet_string_pattern_invalid.yaml", Some("facet_string_pattern_invalid.report"))
  }

  test("Facet string length valid") {
    validate("facet_string_length_valid.yaml")
  }

  test("Extensions") {
    validate("extensions.yaml")
  }

  test("Security definitions") {
    validate("security_definitions.yaml")
  }

  test("Full API check") {
    validate("full_check.json")
  }

  test("JSON Schema include") {
    validate("json_schema_include.yaml")
  }

  test("Documentation info") {
    validate("documentation_info.json")
  }
}
