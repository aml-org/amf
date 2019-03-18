package amf.validation
import amf.core.remote.{Hint, RamlYamlHint}

class RamlExtendsValidationTest extends UniquePlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/extends/"
  override val hint: Hint          = RamlYamlHint

  // Closed shape

  test("Invalid resource type with closed shape") {
    validate("/resource_types/invalid/resource-type-closed-shape.raml", Some("resource-type-closed-shape.report"))
  }

  test("Invalid resource type with closed parametrized shape") {
    validate("/resource_types/invalid/resource-type-closed-parametrized-shape.raml",
             Some("resource-type-closed-parametrized-shape.report"))
  }

  test("Invalid trait with closed shape") {
    validate("/traits/invalid/trait-closed-shape.raml", Some("trait-closed-shape.report"))
  }

  test("Invalid trait with closed parametrized shape") {
    validate("/traits/invalid/trait-closed-parametrized-shape.raml", Some("trait-closed-parametrized-shape.report"))
  }

  // Missing parameters

  test("Missing parameter in trait key") {
    validate("/traits/invalid/trait-missing-parameter-in-key.raml", Some("trait-missing-parameter-in-key.report"))
  }

  test("Missing parameter in trait value") {
    validate("/traits/invalid/trait-missing-parameter-in-value.raml", Some("trait-missing-parameter-in-value.report"))
  }

  test("Missing parameter in resource type key") {
    validate("/resource_types/invalid/resource-type-missing-parameter-in-key.raml",
             Some("resource-type-missing-parameter-in-key.report"))
  }

  test("Missing parameter in resource type value") {
    validate("/resource_types/invalid/resource-type-missing-parameter-in-value.raml",
             Some("resource-type-missing-parameter-in-value.report"))
  }

  // Optional methods

  test("Closed shape in applied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-shape-in-applied-optional-method.raml",
      Some("resource-type-closed-shape-in-applied-optional-method.report")
    )
  }

  test("Closed shape in unapplied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-shape-in-unapplied-optional-method.raml",
      Some("resource-type-closed-shape-in-unapplied-optional-method.report")
    )
  }

  test("Closed parametrized shape in applied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-parametrized-shape-in-applied-optional-method.raml",
      Some("resource-type-closed-parametrized-shape-in-applied-optional-method.report")
    )
  }

  test("Closed parametrized shape in unapplied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-parametrized-shape-in-unapplied-optional-method.raml",
      Some("resource-type-closed-parametrized-shape-in-unapplied-optional-method.report")
    )
  }

  // Advanced parameters

  test("Advanced parameter cases in resource types") {
    validate("/resource_types/resource-type-advanced-parameter-cases.raml",
             Some("resource-type-advanced-parameter-cases.report"))
  }

  test("Advanced parameter cases in traits") {
    validate("/traits/trait-advanced-parameter-cases.raml", Some("trait-advanced-parameter-cases.report"))
  }

  // Complex cases
  test("Complex case with closed shape validations") {
    validate("/extends/complex-cases/complexCasesWithClosedShapeValidations.raml",
             Some("complex-case-with-closed-shape-validations.report"))
  }

//  test("nestedTraitsInResourceTypes.raml") {
//    validate("/extends/complex-cases/nestedTraitsInResourceType.raml", Some("blank.report")) //TODO
//  }

  test("Complex parametrized cases") {
    validate("/extends/complex-cases/complexParametrizedCases.raml", None)
  }

}
