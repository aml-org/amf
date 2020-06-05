package amf.validation

import amf.Async20Profile
import amf.core.remote.{AsyncYamlHint, Hint}
import org.scalatest.Matchers

class Async20MultiPlatformValidationsTest extends MultiPlatformReportGenTest with Matchers {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/async20/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/async20/"
  override val hint: Hint          = AsyncYamlHint

  test("Draft 7 - conditional sub schemas validations") {
    validate("draft-7-validations.yaml", Some("draft-7-validations.report"), Async20Profile)
  }

  test("invalid examples defined in variables of server") {
    validate("invalid-server-variable-examples.yaml", Some("invalid-server-variable-examples.report"), Async20Profile)
  }
}
