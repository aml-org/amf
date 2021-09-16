ModelId: file://amf-cli/shared/src/test/resources/validations/recursives/props1.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a.a.b should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/props1.raml#/declarations/types/A/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/props1.raml#/declarations/types/A/example/invalid
  Range: [(16,0)-(21,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/props1.raml
