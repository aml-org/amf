Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/max-exclusive-schema.raml
Profile: RAML 0.8
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be < 180
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/max-exclusive-schema.raml#/declares/shape/invalidExample/property/property/prop1/scalar/prop1/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/max-exclusive-schema.raml#/declares/shape/invalidExample/property/property/prop1/scalar/prop1/examples/example/default-example
  Position: Some(LexicalInformation([(20,36)-(20,39)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/max-exclusive-schema.raml
