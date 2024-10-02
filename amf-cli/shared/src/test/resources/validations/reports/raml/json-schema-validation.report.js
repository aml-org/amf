ModelId: file://amf-cli/shared/src/test/resources/validations/raml/json-schema-validation/lib.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'flightId'
should match "then" schema

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/json-schema-validation/lib.raml#/declares/shape/updateFlight/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/raml/json-schema-validation/lib.raml#/declares/shape/updateFlight/examples/example/default-example
  Range: [(8,0)-(10,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/raml/json-schema-validation/lib.raml
