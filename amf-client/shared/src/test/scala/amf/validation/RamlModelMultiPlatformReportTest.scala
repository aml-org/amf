package amf.validation
import amf.{Raml08Profile, Raml10Profile}
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

  test("Annotations are validated when contained in scalar valued nodes") {
    validate("annotations/annotating-scalar-valued-nodes.raml", Some("annotating-scalar-valued-nodes.report"))
  }

  test("Invalid values in user defined facets") {
    validate("facets/invalid-facet-value-type.raml", Some("invalid-facet-value-type.report"))
  }

  test("Raml overlay with invalid example without overloading type") {
    validate("overlays/overlay-with-inferred-type-invalid-example/overlay.raml",
             Some("overlay-with-inferred-type-invalid-example.report"),
             Raml10Profile)
  }

  test("Annotation type with invalid example") {
    validate("annotation-types-invalid-example.raml", Some("annotation-types-invalid-example.report"), Raml10Profile)
  }

  test("nested json schema defined in external fragment") {
    validate("raml/nested-json-schema-external-fragment/api.raml",
             Some("invalid-example-result.report"),
             Raml08Profile)
  }

  test("maximum/minimum validation with 17 digit numbers") {
    validate("raml/big-number-examples.raml", Some("invalid-big-numbers.report"), Raml10Profile)
  }

  override val basePath    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath = "amf-client/shared/src/test/resources/validations/reports/multi-plat-model/"
  override val hint: Hint  = RamlYamlHint
}
