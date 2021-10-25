package amf.pce

import amf.core.remote.{Hint, RamlYamlHint}
import amf.validation.MultiPlatformReportGenTest

class PceTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/pce/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/pce/"
  override val hint: Hint          = RamlYamlHint

  test("Valid - Test schema dependencies validate correctly for RAML 1.0") {
    validate("valid-schema-dependencies.raml", None)
  }

  test("Invalid - Test schema dependencies validate correctly for RAML 1.0") {
    validate("invalid-schema-dependencies.raml", Some("invalid-schema-dependencies.report"))
  }

  test("Valid - Test property dependencies validate correctly for RAML 1.0") {
    validate("valid-property-dependencies.raml", None)
  }

  test("Invalid - Test property dependencies validate correctly for RAML 1.0") {
    validate("invalid-property-dependencies.raml", Some("invalid-property-dependencies.report"))
  }
}
