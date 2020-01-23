package amf.validation
import amf.core.remote.{Hint, RamlYamlHint}

class RamlJapaneseCharsetValidationTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/japanese/raml/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/japanese/raml/"
  override val hint: Hint          = RamlYamlHint

  test("Documentation title and content") {
    validate("documentation-title-content.raml")
  }

  test("Documentation baseUriParameters description") {
    validate("documentation-base-uri-params.raml")
  }

  test("Documentation resource description") {
    validate("documentation-resource-description.raml")
  }

  test("Documentation resource display names") {
    validate("documentation-resource-display-names.raml")
  }

  test("Documentation security schemes description") {
    validate("documentation-security-schemes.raml")
  }

  test("Documentation security schemes header description") {
    validate("documentation-security-schemes-headers.raml")
  }

  test("Documentation security schemes query params") {
    validate("documentation-security-schemes-query-params.raml")
  }

  test("Documentation comment inline") {
    validate("documentation-comment-inline.raml")
  }

  test("Documentation comment single line") {
    validate("documentation-comment-single-line.raml")
  }

  test("Documentation library usage") {
    validate("documentation-library-usage.raml")
  }

  test("Facet string pattern valid") {
    validate("facet-string-pattern-valid.raml")
  }

  test("Facet string pattern invalid") {
    validate("facet-string-pattern-invalid.raml", Some("facet-string-pattern-invalid.report"))
  }

  test("Facet string min length valid") {
    validate("facet-string-length-valid.raml")
  }

  test("Facet string min length invalid") {
    validate("facet-string-length-invalid.raml", Some("facet-string-length-invalid.report"))
  }

  test("Complex type with object type") {
    validate("complex-type.raml")
  }

  test("JSON Schema include") {
    validate("json-schema-include.raml")
  }

  test("Full API check") {
    validate("full-check.raml")
  }

  test("Annotations") {
    validate("annotations.raml")
  }

  test("Root title") {
    validate("root-title.raml")
  }

  test("Optional parameters") {
    validate("optional-parameters.raml")
  }

  test("Declaring traits and resource types") {
    validate("traits-and-resource-types.raml")
  }

  test("Non ascii header names") {
    validate("non-ascii-headers.raml", Some("non-ascii-headers.report"))
  }
}
