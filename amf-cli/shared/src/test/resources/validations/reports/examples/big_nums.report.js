ModelId: file://amf-cli/shared/src/test/resources/validations/types/big_nums.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 999999999999
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/types/big_nums.raml#/declarations/types/scalar/LongNumber/example/three
  Property: file://amf-cli/shared/src/test/resources/validations/types/big_nums.raml#/declarations/types/scalar/LongNumber/example/three
  Range: [(11,13)-(11,26)]
  Location: file://amf-cli/shared/src/test/resources/validations/types/big_nums.raml
