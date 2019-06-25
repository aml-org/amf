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

  test("Facet minimum and maximum with left zeros") {
    validate("/facets/min-max-zeros.raml", Some("min-max-zeros.report"))
  }

  test("Facet minimum and maximum with left zeros other") {
    validate("/facets/min-max-zeros-other.raml", Some("min-max-zeros-other.report"))
  }

  test("Discriminator with closed parent") {
    validate("discriminator/invalid/closed-parent.raml", Some("discriminator-closed-parent.report"))
  }

  test("Discriminator with additional enum values") {
    validate("discriminator/invalid/additional-enum-values.raml", Some("discriminator-additional-enum-values.report"))
  }

  test("Discriminator in array items") {
    validate("discriminator/discriminator-array-items.raml", Some("discriminator-array-items.report"))
  }

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/multi-plat-model/"
  override val hint: Hint  = RamlYamlHint
}
