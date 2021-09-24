ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have additional properties
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml#/declares/shape/Animal/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml#/declares/shape/Animal/examples/example/default-example
  Range: [(18,0)-(20,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml
