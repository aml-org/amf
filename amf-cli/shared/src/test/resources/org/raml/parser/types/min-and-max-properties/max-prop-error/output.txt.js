ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/types/min-and-max-properties/max-prop-error/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 2 properties
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/min-and-max-properties/max-prop-error/input.raml#/declares/shape/Initial_comments/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/min-and-max-properties/max-prop-error/input.raml#/declares/shape/Initial_comments/examples/example/default-example
  Range: [(12,0)-(15,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/min-and-max-properties/max-prop-error/input.raml
