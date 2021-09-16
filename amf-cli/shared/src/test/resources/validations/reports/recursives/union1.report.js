ModelId: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be string
b.b should be string
c should be object
c should be string
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml#/declarations/types/any/A/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml#/declarations/types/any/A/example/invalid
  Range: [(12,0)-(15,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml
