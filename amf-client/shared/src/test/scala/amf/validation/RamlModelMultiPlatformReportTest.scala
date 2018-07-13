package amf.validation
import amf.core.remote.{Hint, RamlYamlHint}

class RamlModelMultiPlatformReportTest extends MultiPlatformReportGenTest {

  test("Test non existing include in type def") {
    validate("/missing-includes/in-type-def.raml", Some("missing-includes/in-type-def.report"))
  }

  test("Test non existing include in resource type def") {
    validate("/missing-includes/in-resource-type-def.raml", Some("missing-includes/in-resource-type-def.report"))
  }

  test("Test non existing include in trait def") {
    validate("/missing-includes/in-trait-def.raml", Some("missing-includes/in-trait-def.report"))
  }

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/multi-plat-model/"
  override val hint: Hint  = RamlYamlHint
}
