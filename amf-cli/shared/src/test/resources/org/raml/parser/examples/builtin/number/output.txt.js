ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/number/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/number/input.raml#/declarations/types/User/example/bad-int
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/number/input.raml#/declarations/types/User/example/bad-int
  Range: [(20,0)-(22,21)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/number/input.raml
