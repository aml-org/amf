package amf.validation

import amf.Raml08Profile
import amf.core.remote.{Hint, RamlYamlHint}

class RamlExamplesValidationTest extends MultiPlatformReportGenTest {

  override val basePath: String    = "file://amf-client/shared/src/test/resources/validations/"
  override val reportsPath: String = "amf-client/shared/src/test/resources/validations/reports/examples/"

  test("Array minCount 1") {
    validate("examples/arrayItems1.raml", Some("array-items1.report"))
  }

  test("Example model validation test") {
    validate("examples/examples_validation.raml", Some("examples_validation.report"))
  }

  // todo: review test ofor warning schema
  test("Validates html example value example1.raml") {
    validate("examples/example1.raml", Some("example1.report"))
  }

  test("Discriminator test 1") {
    validate("examples/discriminator1.raml", Some("discriminator1.report"))
  }

  test("Discriminator test 2") {
    validate("examples/discriminator2.raml", Some("discriminator2.report"))
  }

  test("Shape facets model validations test") {
    validate("facets/custom-facets.raml", Some("custom-facets.report"))
  }

  test("Annotations validations test") {
    validate("annotations/annotations.raml", Some("annotations.report"))
  }

  test("Annotations enum validations test") {
    validate("annotations/annotations_enum.raml", Some("annotations_enum.report"))
  }

  test("MinLength, maxlength facets validations test") {
    validate("types/lengths.raml", Some("min-max-length.report"))
  }

  test("big numbers validations test") {
    validate("types/big_nums.raml", Some("big_nums.report"))
  }

  test("Parse and validate named examples as external framents") {
    validate("examples/inline-named-examples/api-for-validate.raml")
  }

  test("Nil value validation") {
    validate("examples/nil_validation.raml", Some("nil-validation.report"))
  }

  test("Example invalid min and max constraint validations test") {
    validate("examples/invalid-max-min-constraint.raml", Some("invalid-max-min-constraint.report"))
  }

  test("Test js custom validation - multiple of") {
    validate("custom-js-validations/mutiple-of.raml", Some("mutiple-of.report"))
  }

  test("Can validate an array example") {
    validate("examples/invalid-property-in-array-items.raml", Some("invalid-property-in-array-items.report"))
  }

  test("Can validate correctly incorrect json schema properties for a certain json schema version") {
    validate("examples/invalid-json-schema-version-properties.raml",
             Some("invalid-json-schema-version-properties.report"))
  }

  test("Param in raml 0.8 api") {
    validate("08/pattern.raml", Some("pattern-08.report"), profile = Raml08Profile)
  }

  test("Validation error raml 0.8 example 1") {
    validate("08/validation_error1.raml", Some("validation_error1.report"), profile = Raml08Profile)
  }

  test("Test validate pattern with invalid example") {
    validate("examples/pattern-invalid.raml", Some("pattern-invalid.report"))
  }

  test("Test failed union ex 1 with invalid example") {
    validate("examples/union1-invalid.raml", Some("union1-invalid.report"))
  }

  // this is not in ParaPayloadValidation test because we need to check the validation against a raml 08 parsed and resolved model (with that profile).
  test("Raml 0.8 Query Parameter Negative test case") {
    validate("/08/date-query-parameter.raml", Some("date-query-parameter.report"), profile = Raml08Profile)
  }

  test("Invalid example validation over union shapes") {
    validate("/shapes/invalid-example-in-unions.raml", Some("example-in-unions.report"))
  }

  test("Test resource type invalid examples args validation") {
    validate("/resource_types/parameterized-references/input.raml", Some("examples-resource-types.report"))
  }

  // In fact, the violation its while resolving the model, not running validation itself
  test("Invalid key in trait test") {
    validate("/traits/trait1.raml", Some("invalid-hey-trait.report"))
  }

  test("Invalid nested endpoint in resource type") {
    validate("/resource_types/nested-endpoint.raml", Some("nested-endpoint.report"))
  }

  test("Test minItems maxItems examples") {
    validate("/examples/min-max-items.raml", Some("min-max-items.report"))
  }

  test("Date times invalid examples test") {
    validate("/examples/date_time_validations2.raml", Some("date-time-validation.report"))
  }

  test("Example xml with sons results test") {
    validate("/xmlexample/offices_xml_type.raml", Some("offices-xml-type.report"))
  }

  test("Invalid number prop with bool example") {
    validate("/examples/number-prop-bool-example.raml", Some("number-prop-bool-example.report"))
  }

  test("Invalid double example for int type") {
    validate("/examples/double-example-inttype.raml", Some("double-example-inttype.report"))
  }

  test("Invalid annotation value") {
    validate("/examples/invalid-annotation-value.raml", Some("invalid-annotation-value.report"))

  }

  test("Test validate declared type from header") {
    validate("/examples/declared-from-header.raml", Some("declared-from-header.report"))

  }

  test("Test maxProperties and minProperties constraints example") {
    validate("/examples/min-max-properties-example.raml", Some("min-max-properties-example.report"))
  }

  test("lock-unlock example test (raml dates)") {
    validate("/examples/raml-dates/lockUnlockStats.raml", Some("raml-dates-lockunlock.report"), Raml08Profile)
  }

  test("Pattern properties key") {
    validate("/examples/pattern-properties/pattern_properties.raml",
             Some("pattern-properties/pattern_properties.report"))
  }

  test("Pattern properties key 2 (all additional properties)") {
    validate("/examples/pattern-properties/pattern_properties2.raml",
             Some("pattern-properties/pattern_properties2.report"))
  }

  test("Pattern properties key 3 (precedence)") {
    validate("/examples/pattern-properties/pattern_properties3.raml",
             Some("pattern-properties/pattern_properties3.report"))
  }

  test("Pattern properties key 4 (additionalProperties: false clash)") {
    validate("/examples/pattern-properties/pattern_properties4.raml",
             Some("pattern-properties/pattern_properties4.report"))
  }

  test("Check binary file included for string shape example") {
    validate("/examples/image-file-example/in-stringshape.raml", Some("image-file-example/in-stringshape.report"))
  }

  test("File examples always validate unless encoding problems are found") {
    validate("/examples/image-file-example/in-fileshape.raml", Some("image-file-example/in-fileshape.report"))
  }

  test("Examples facet with an array instead a map") {
    validate("/examples/examples-array-not-map.raml", Some("examples-array-not-map.report"))
  }

  // is here this test ok? or i should move to another new test suit
  test("Test validate default value") {
    validate("/examples/invalid-default.raml", Some("invalid-default.report"))
  }

  test("Test invalid example in object against enum") {
    validate("/enums/invalid-obj-example-enum.raml", Some("invalid-obj-example-enum.report"))
  }

  test("Test invalid example in array against enum") {
    validate("/enums/invalid-array-enums.raml", Some("invalid-array-enums.report"))
  }

  test("Test invalid example in obj array against enum") {
    validate("/enums/invalid-obj-array-enums.raml", Some("invalid-obj-array-enums.report"))
  }

  test("Test invalid obj enum value") {
    validate("/enums/invalid-obj-enum.raml", Some("invalid-obj-enum.report"))
  }

  test("Test ints formats validations") {
    validate("/types/int-formats.raml", Some("int-formats.report"))
  }

  test("Test invalid int64 format with int example") {
    validate("/examples/invalid-format-example.raml")
  }

  test("Test invalid string hierarchy examples") {
    validate("/examples/string-hierarchy.raml", Some("scalars-numbers-string.report"))
  }

  test("Recursion with unresolve ref - validate recursion") {
    validate("/shapes/soho-prod/api.raml")
  }

  test("Nullable example (null disjoint union)") {
    validate("/examples/nullable-schema/api.raml")
  }

  // validate enums here?
  test("Invalid boolean enum value at string type") {
    validate("enums/enum-boolean-invalid.raml", Some("enum-boolean-invalid.report"))
  }

  test("Invalid integer enum value at string type") {
    validate("enums/enum-int-invalid.raml", Some("enum-int-invalid.report"))
  }

  test("External json example of type") {
    validate("/examples/external-json/api.raml", Some("external-json-example.report"))
  }

  test("External frag example from example facet") {
    validate("/examples/inlined-external-example/api.raml")
  }

  test("Complex inheritance with example") {
    validate("/examples/complex-inheritance/api.raml", Some("complex-inheritance.report"))
  }

  test("NamedExample fragment in example facet") {
    validate("/examples/named-example-facet/api.raml")
  }

  test("NamedExample fragment in example facet with 'value' key") {
    validate("/examples/named-example-facet-value/api.raml")
  }

  test("NamedExample fragment in example facet with other key") {
    validate("/examples/named-example-facet-other/api.raml", Some("named-example-facet-other.report"))
  }

  test("NamedExample fragment that includes another NamedExample fragment") {
    validate("/examples/named-example-chained/api.raml")
  }

  test("NamedExample fragment with invalid example with double key") {
    validate("/examples/named-example-double-key/api.raml", Some("named-example-double-key.report"))
  }

  test("NamedExample fragment with multiple examples in example facet") {
    validate("/examples/named-example-multiple/api.raml", Some("named-example-multiple.report"))
  }

  test("Shape with multiple includes of NamedExamples") {
    validate("/examples/named-example-multiple-include/api.raml")
  }

  test("NamedExample fragment in examples facet with 'value' key") {
    validate("/examples/named-examples-facet-value/api.raml")
  }

  test("NamedExample fragment in examples facet without key") {
    validate("/examples/named-examples-facet-no-key/api.raml", Some("named-examples-facet-no-key.report"))
  }

  test("NamedExample fragment in examples complex") {
    validate("/examples/named-examples-value-complex/api.raml")
  }

  test("NamedExample fragment with a sequence in example") {
    validate("/examples/named-examples-seq/api.raml")
  }

  test("NamedExample fragment with invalid sequence in example") {
    validate("/examples/named-examples-seq-invalid/api.raml", Some("named-examples-seq-invalid.report"))
  }

  test("Shape with single inheritance but no simple") {
    validate("/examples/single-complex-inheritance.raml", Some("single-complex-inheritance.report"))
  }

  test("Shape cross recursion") {
    validate("/examples/cross-recursion.raml")
  }

  test("Inherit in scalars should reject inherited examples") {
    validate("/examples/inherits-reject-examples.raml")
  }

  override val hint: Hint = RamlYamlHint
}
