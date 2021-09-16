ModelId: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have fewer than 2 items
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declarations/types/array/Colors/example/bad-min
  Property: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declarations/types/array/Colors/example/bad-min
  Range: [(10,15)-(10,21)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 3 items
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declarations/types/array/Colors/example/bad-max
  Property: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declarations/types/array/Colors/example/bad-max
  Range: [(11,15)-(11,39)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml
