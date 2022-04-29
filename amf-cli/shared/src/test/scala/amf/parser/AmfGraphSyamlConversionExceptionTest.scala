package amf.parser

import amf.core.internal.remote.{AmfJsonHint, Hint}
import amf.validation.{MultiPlatformReportGenTest, UniquePlatformReportGenTest}

class UniqueAmfGraphSyamlConversionExceptionTest extends UniquePlatformReportGenTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/conversion-exceptions/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/conversion-exceptions/reports/"
  override val hint: Hint          = AmfJsonHint

  test("Invalid @type array is not all strings") {
    validate("invalid-type-array-is-not-all-string.jsonld", Some("invalid-type-array-is-not-all-string.report"))
  }

  test("JSON-LD value conversion uses error handler 1") {
    validate("invalid-root-field-value.jsonld", Some("invalid-root-field-value.report"))
  }

  test("JSON-LD value conversion uses error handler 2") {
    validate("invalid-version-value.jsonld", Some("invalid-version-value.report"))
  }

  test("Invalid expanded context entry id format") {
    validate("invalid-expanded-context-entry-id-format.jsonld", Some("invalid-expanded-context-entry-id-format.report"))
  }

  test("Invalid expanded context entry type format") {
    validate(
      "invalid-expanded-context-entry-type-format.jsonld",
      Some("invalid-expanded-context-entry-type-format.report")
    )
  }
}

class MultiAmfGraphSyamlConversionExceptionTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-cli/shared/src/test/resources/validations/conversion-exceptions/"
  override val reportsPath: String = "amf-cli/shared/src/test/resources/validations/conversion-exceptions/reports/"
  override val hint: Hint          = AmfJsonHint

  test("Invalid graph dependency value") {
    validate("invalid-graph-dependency-value.jsonld", Some("invalid-graph-dependency-value.report"))
  }
}
