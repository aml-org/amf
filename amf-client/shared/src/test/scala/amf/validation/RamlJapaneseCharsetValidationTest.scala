package amf.validation
import amf.core.remote.{Hint, RamlYamlHint}

class RamlJapaneseCharsetValidationTest extends UniquePlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/japanese/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/japanese/"
  override val hint: Hint          = RamlYamlHint

  test("Documentation title and content") {
    validate("documentation_title_content.raml")
  }

  test("Documentation baseUriParameters description") {
    validate("documentation_base_uri_params.raml")
  }

  test("Documentation resource description") {
    validate("documentation_resource_description.raml")
  }

  test("Documentation resource display names") {
    validate("documentation_resource_display_names.raml")
  }

  test("Documentation security schemes description") {
    validate("documentation_security_schemes.raml")
  }

  test("Documentation security schemes header description") {
    validate("documentation_security_schemes_headers.raml")
  }

  test("Documentation security schemes query params") {
    validate("documentation_security_schemes_query_params.raml")
  }

  test("Documentation comment inline") {
    validate("documentation_comment_inline.raml")
  }

  test("Documentation comment single line") {
    validate("documentation_comment_single_line.raml")
  }

  test("Documentation library usage") {
    validate("documentation_library_usage.raml")
  }

  test("Facet string pattern valid") {
    validate("facet_string_pattern_valid.raml")
  }

  test("Facet string pattern invalid") {
    validate("facet_string_pattern_invalid.raml", Some("facet_string_pattern_invalid.report"))
  }

  test("Facet string min length valid") {
    validate("facet_string_length_valid.raml")
  }

  test("Facet string min length invalid") {
    validate("facet_string_length_invalid.raml", Some("facet_string_length_invalid.report"))
  }

  test("Complex type with object type") {
    validate("complex_type.raml")
  }

  test("JSON Schema include") {
    validate("json_schema_include.raml")
  }

  test("Full API check") {
    validate("full_check.raml")
  }

  test("Annotations") {
    validate("annotations.raml")
  }

  test("Root title") {
    validate("root_title.raml")
  }
}
