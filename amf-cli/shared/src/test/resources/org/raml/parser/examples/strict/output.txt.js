ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/strict/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/strict/input.raml#/declares/scalar/two/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/strict/input.raml#/declares/scalar/two/examples/example/default-example
  Range: [(14,19)-(14,21)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/strict/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/strict/input.raml#/declares/scalar/three/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/strict/input.raml#/declares/scalar/three/examples/example/default-example
  Range: [(17,17)-(17,19)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/strict/input.raml
