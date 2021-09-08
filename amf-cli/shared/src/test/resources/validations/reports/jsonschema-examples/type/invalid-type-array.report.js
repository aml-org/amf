Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/type/invalid-type-array.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be null
should be string
should match some schema in anyOf

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/type/invalid-type-array.raml#/declares/shape/testType/property/property/testProp/union/testProp/examples/example/default-example_2
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/type/invalid-type-array.raml#/declares/shape/testType/property/property/testProp/union/testProp/examples/example/default-example_2
  Position: Some(LexicalInformation([(17,19)-(17,22)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/type/invalid-type-array.raml
