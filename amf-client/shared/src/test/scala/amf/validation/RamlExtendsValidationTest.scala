package amf.validation
import amf.Raml08Profile
import amf.core.remote.{Hint, RamlYamlHint}

class RamlUniquePlatformExtendsValidationTest extends UniquePlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/extends/"
  override val hint: Hint          = RamlYamlHint

  // Closed shape

  test("Resource type with closed shape") {
    validate("/resource_types/invalid/resource-type-closed-shape.raml", Some("resource-type-closed-shape.report"))
  }

  test("Resource type with closed shape from parameter") {
    validate("/resource_types/invalid/resource-type-closed-parametrized-shape.raml",
             Some("resource-type-closed-parametrized-shape.report"))
  }

  test("Trait with closed shape") {
    validate("/traits/invalid/trait-closed-shape.raml", Some("trait-closed-shape.report"))
  }

  test("Trait with closed shape from parameter") {
    validate("/traits/invalid/trait-closed-parametrized-shape.raml", Some("trait-closed-parametrized-shape.report"))
  }

  test("Nested parameters with closed shape") {
    validate("/extends/nestedParametersWithClosedShape.raml", Some("nestedParametersWithClosedShape.report"))
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

  test("Closed shape from parameter in applied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-parametrized-shape-in-applied-optional-method.raml",
      Some("resource-type-closed-parametrized-shape-in-applied-optional-method.report")
    )
  }

  test("Closed shape from parameter in unapplied optional method") {
    validate(
      "/resource_types/invalid/optional-methods/resource-type-closed-parametrized-shape-in-unapplied-optional-method.raml",
      Some("resource-type-closed-parametrized-shape-in-unapplied-optional-method.report")
    )
  }

  // Special parameter cases (null, string with spaces, etc.)

  test("Advanced parameter cases in resource types") {
    validate("/resource_types/resource-type-advanced-parameter-cases.raml",
             Some("resource-type-advanced-parameter-cases.report"))
  }

  test("Advanced parameter cases in traits") {
    validate("/traits/trait-advanced-parameter-cases.raml", Some("trait-advanced-parameter-cases.report"))
  }

  // Complex cases

  test("Nested case with closed shape validations") {
    validate("/extends/complex-cases/complexCasesWithClosedShapeValidations.raml",
             Some("complex-case-with-closed-shape-validations.report"))
  }

  ignore("Non applied extends declarations with closed shape validations") {
    // TODO this conforms because we don't validate RTs and Traits if they are not used
    validate("/extends/complex-cases/nonAppliedExtendsDeclarationsWithClosedShapeValidations.raml", None)
  }

  test("Complex parametrized cases") {
    validate("/extends/complex-cases/complexParametrizedCases.raml", Some("complexParametrizedCases.report"))
  }

  test("Resource Type operation without mediaType and without global mediaType") {
    validate("/extends/rt-no-mediatype.raml", Some("rt-no-mediatype.report"))
  }

  test("Resource Type operation without mediaType and with global mediaType") {
    validate("/extends/rt-global-mediatype.raml", None)
  }

  // Merge payloads

  test("Single media type defined unequally in request") {
    validate("/extends/merging-payloads/media-type-single-request/unequallyDefined.raml",
             Some("unequallyDefinedPayloads.report"))
  }

  test("Single media type defined unequally in response") {
    validate("/extends/merging-payloads/media-type-single-response/unequallyDefined.raml",
             Some("unequallyDefinedPayloadsResponse.report"))
  }

  test("Multiple media types defined unequally") {
    validate("/extends/merging-payloads/media-type-multiple/unequallyDefinedMultiple.raml",
             Some("unequallyDefinedMultiplePayloads.report"))
  }

  test("Merge payloads in nested extends") {
    validate("/extends/merging-payloads/multiple-merges/multipleMerges.raml",
             Some("payloadMergingMultipleMerges.report"))
  }

  test("Merging extends with no payload") {
    validate("/extends/merging-payloads/merging-extends-with-no-payload.raml",
             Some("merging-extends-with-no-payload.report"))
  }

  test("Merge extends with empty payload") {
    validate("/extends/merging-payloads/empty-payload.raml", None)
  }

  // References

  test("Inexistent references in non optional operations") {
    validate(
      "/extends/references/resource-types/non-parametrized-non-optional-inexistent.raml",
      Some("references/resource-types/non-parametrized-non-optional-inexsistent.report")
    )
  }

  test("Inexistent references in optional operations") {
    validate(
      "/extends/references/resource-types/non-parametrized-optional-inexistent.raml",
      Some("references/resource-types/non-parametrized-optional-inexsistent.report")
    )
  }

  test("Inexistent references from parameters in optional operations") {
    validate("/extends/references/resource-types/parametrized-optional-inexistent.raml",
             Some("references/resource-types/parametrized-optional-inexsistent.report"))
  }

  test("Inexistent reference in trait") {
    validate("/extends/references/traits/non-parametrized.raml", Some("references/traits/non-parametrized.report"))
  }

  test("Inexistent reference from parameter in trait") {
    validate("/extends/references/traits/parametrized.raml", Some("references/traits/parametrized.report"))
  }

  test("Nested parameter reference") {
    validate("/extends/references/nestedParameter.raml", Some("references/nestedParameter.report"))
  }

  test("Existent includes") {
    validate("/extends/references/existent-includes/api.raml", None)
  }

  test("Nested resource types with operations") {
    validate("/extends/references/nestedResourceTypesWithOperations.raml", None)
  }

  test("Nested resource types with inexistent references") {
    validate(
      "/extends/references/nestedResourceTypesWithInexistentReferences.raml",
      Some("references/resource-types/nestedResourceTypesWithInexistentReferences.report")
    )
  }

  test("Nested traits and inexistent trait") {
    validate("/extends/references/nestedTraitsWithInexistentTrait.raml",
             Some("references/nestedTraitsWithInexistentTrait.report"))
  }

  test("Nested resource types and inexistent resource type") {
    validate("/extends/references/nestedRTsWithInexistentRT.raml", Some("references/nestedRTsWithInexistentRT.report"))
  }

  test("Crossed library references") {
    validate("/extends/references/crossed-libraries/api.raml", Some("references/crossed-libraries.report"))
  }

  // Examples validation

  test("Optional method as parameter value with inexistent reference") {
    validate(
      "/resource_types/optionalMethodAsParameterValueWithInexistentReference.raml",
      Some("resource-types/optionalMethodAsParameterValueWithInexistentReference.report")
    )
  }

  // Duplicate keys

  ignore("Duplicate keys in extends coming from parameters") {
    validate("/extends/duplicateKeysFromParameters.raml", Some("duplicateKeysFromParameters.report"))
  } // TODO

  // Typed fragments

  test("Resource type in typed fragment") {
    validate("/extends/typed-fragments/resource-types/api.raml", Some("typed-fragments-resource-types.report"))
  }

  test("Traits in typed fragment") {
    validate("/extends/typed-fragments/traits/api.raml", Some("typed-fragments-traits.report"))
  }

  // Merging optional RAML 0.8 nodes

  test("Merging optional nodes in resource types") {
    validate("/extends/optional-raml08-nodes/rts.raml", profile = Raml08Profile)
  }

  test("Merging optional nodes in traits") {
    validate("/extends/optional-raml08-nodes/traits.raml", profile = Raml08Profile)
  }

  test("Uri parameter in resource type") {
    validate("/extends/uri-parameters/resource.raml", Some("uri-parameters-in-rt.report"))
  }

  test("Uri parameter in multi-level resource type") {
    validate("/extends/uri-parameters/multi-level-rts.raml", Some("uri-parameters-in-multilevel-rt.report"))
  }

  test("Uri parameter in multi-level endpoints") {
    validate("/extends/uri-parameters/multi-level-endpoints.raml",
             Some("uri-parameters-in-multilevel-rt-with-multilevel-endpoints.report"))
  }

  // Merging security schemes
  test("Merging security schemes in RAML 0.8"){
    validate("extends/raml08-with-security-schemes-in-trait.raml")
  }

  test("Merging security schemes in RAML 1.0"){
    validate("extends/raml10-with-security-schemes-in-trait.raml")
  }

}

class RamlMultiPlatformExtendsValidationTest extends MultiPlatformReportGenTest {
  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/extends/"
  override val hint: Hint          = RamlYamlHint

  // Payload merging

  test("Single media type defined equally in request") {
    validate("/extends/merging-payloads/media-type-single-request/equallyDefined.raml",
             Some("equallyDefinedPayloads.report"))
  }

  test("Single media type defined equally in response") {
    validate("/extends/merging-payloads/media-type-single-response/equallyDefined.raml",
             Some("equallyDefinedPayloadsResponse.report"))
  }

  test("Multiple media types defined equally") {
    validate("/extends/merging-payloads/media-type-multiple/equallyDefinedMultiple.raml",
             Some("equallyDefinedMultiplePayloads.report"))
  }

  // References

  test("Inexistent includes") {
    validate("/extends/references/nonExistentIncludes.raml", Some("references/nonExistentIncludes.report"))
  }

  test("Including and applying non existent resource type") {
    validate("/resource_types/non-existent-include.raml", Some("/resource-types/non-existent-include.report"))
  }

  test("Including and applying non existent traits") {
    validate("/traits/non-existent-include.raml", Some("/traits/non-existent-include.report"))
  }

  // Examples validation

  test("Optional method as parameter value") {
    validate("/resource_types/optionalMethodAsParameterValue.raml",
             Some("resource-types/optionalMethodAsParameterValue.report"))
  }

  test("Examples validations in resource types") {
    validate("/resource_types/examplesValidation.raml", Some("resource-types/examplesValidation.report"))
  }

  test("Parametrized includes in examples tag") {
    validate("/resource_types/parametrized-includes-of-examples/api.raml",
             Some("resource-types/parametrizedIncludesOfExamples.report"))
  }
}
