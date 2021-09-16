ModelId: file://amf-cli/shared/src/test/resources/validations/enums/invalid-array-enums.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid1
  Property: file://amf-cli/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid1
  Range: [(10,16)-(10,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/invalid-array-enums.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid2
  Property: file://amf-cli/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid2
  Range: [(11,16)-(11,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/invalid-array-enums.raml
