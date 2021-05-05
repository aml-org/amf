package amf.validation

import amf.{Oas20Profile, Oas30Profile}
import amf.core.remote.{Hint, Oas20YamlHint, Oas30JsonHint, Oas30YamlHint}

// TODO: Add test to check for Japanese characters in OAS 3 Component schema name. @author: Tomás Fernández
class OasJapaneseCharsetValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/japanese/oas/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/japanese/oas/"
  override val hint: Hint          = Oas20YamlHint

  test("Documentation server params") {
    validate("documentation-server-params.json", profile = Oas30Profile, overridedHint = Some(Oas30JsonHint))
  }

  test("Documentation path and response description") {
    validate("documentation-path-and-response-description.json",
             profile = Oas30Profile,
             overridedHint = Some(Oas30JsonHint))
  }

  test("Documentation path item description") {
    validate("documentation-path-item-description.json", profile = Oas30Profile, overridedHint = Some(Oas30JsonHint))
  }

  test("Documentation security schemes") {
    validate("documentation-security-schemes.json", profile = Oas30Profile, overridedHint = Some(Oas30JsonHint))
  }

  test("Facet string pattern valid") {
    validate("facet-string-pattern-valid.yaml", profile = Oas30Profile, overridedHint = Some(Oas30YamlHint))
  }

  test("Facet string pattern invalid") {
    validate("facet-string-pattern-invalid.yaml",
             Some("facet-string-pattern-invalid.report"),
             profile = Oas30Profile,
             overridedHint = Some(Oas30YamlHint))
  }

  test("Facet string length valid") {
    validate("facet-string-length-valid.yaml", profile = Oas30Profile, overridedHint = Some(Oas30YamlHint))
  }

  test("Extensions") {
    validate("extensions.yaml", profile = Oas30Profile, overridedHint = Some(Oas30YamlHint))
  }

  test("Security definitions") {
    validate("security-definitions.yaml", profile = Oas20Profile)
  }

  test("Full API check") {
    validate("full-check.json", profile = Oas20Profile)
  }

  test("JSON Schema include") {
    validate("json-schema-include.yaml", profile = Oas30Profile, overridedHint = Some(Oas30YamlHint))
  }

  test("Documentation info") {
    validate("documentation-info.json", profile = Oas30Profile, overridedHint = Some(Oas30JsonHint))
  }

  test("Non ASCII header names OAS 2.0") {
    validate("non-ascii-headers.json", Some("non-ascii-headers.report"), profile = Oas20Profile)
  }

  test("Non ASCII header names OAS 3.0") {
    validate("non-ascii-headers-oas3.json",
             Some("non-ascii-headers-oas3.report"),
             profile = Oas30Profile,
             overridedHint = Some(Oas30JsonHint))
  }
}
