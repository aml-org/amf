ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/array/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [1].active should be boolean
[1].address should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/array/input.raml#/declares/array/Emails/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/array/input.raml#/declares/array/Emails/examples/example/default-example
  Range: [(11,0)-(14,25)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/array/input.raml
