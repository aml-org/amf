ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/types/datetime/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date-time"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/datetime/input.raml#/declares/scalar/when/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/datetime/input.raml#/declares/scalar/when/examples/example/default-example
  Range: [(6,17)-(6,24)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/datetime/input.raml
