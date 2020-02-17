package amf.validation

import amf.core.remote.{Hint, OasYamlHint}
import amf.{Oas20Profile, Oas30Profile}
import org.scalatest.Matchers

class Oas20UniquePlatformUnitValidationsTest extends UniquePlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/oas2/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/oas2/"
  override val hint: Hint          = OasYamlHint

  test("Invalid security scheme type in Oas 20") {
    validate("invalid-security-scheme-type.json", Some("invalid-security-scheme-type.report"), Oas20Profile)
  }

  test("invalid ref inside paths object") {
    validate("invalid-ref-inside-paths-object.json", Some("invalid-ref-inside-paths-object.report"), Oas30Profile)
  }
}
