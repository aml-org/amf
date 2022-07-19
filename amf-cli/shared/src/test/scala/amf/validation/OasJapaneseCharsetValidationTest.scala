package amf.validation

import amf.core.client.common.validation.{Oas20Profile, Oas30Profile}
import amf.core.internal.remote.{Hint, Oas20YamlHint, Oas30JsonHint, Oas30YamlHint}

// TODO: Add test to check for Japanese characters in OAS 3 Component schema name. @author: Tomás Fernández
class OasJapaneseCharsetValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/japanese/oas/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/reports/japanese/oas/"

  test("Documentation server params") {
    validate("documentation-server-params.json")
  }

  test("Documentation path and response description") {
    validate(
      "documentation-path-and-response-description.json"
    )
  }

  test("Documentation path item description") {
    validate("documentation-path-item-description.json")
  }

  test("Documentation security schemes") {
    validate("documentation-security-schemes.json")
  }

  test("Facet string pattern valid") {
    validate("facet-string-pattern-valid.yaml")
  }

  test("Facet string pattern invalid") {
    validate(
      "facet-string-pattern-invalid.yaml",
      Some("facet-string-pattern-invalid.report")
    )
  }

  test("Facet string length valid") {
    validate("facet-string-length-valid.yaml")
  }

  test("Extensions") {
    validate("extensions.yaml")
  }

  test("Security definitions") {
    validate("security-definitions.yaml")
  }

  test("Full API check") {
    validate("full-check.json")
  }

  test("JSON Schema include") {
    validate("json-schema-include.yaml")
  }

  test("Documentation info") {
    validate("documentation-info.json")
  }

  test("Non ASCII header names OAS 2.0") {
    validate("non-ascii-headers.json", Some("non-ascii-headers.report"))
  }

  test("Non ASCII header names OAS 3.0") {
    validate(
      "non-ascii-headers-oas3.json",
      Some("non-ascii-headers-oas3.report")
    )
  }
}
