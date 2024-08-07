ModelId: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml#/declares/array/A/examples/example/invalid1
  Property: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml#/declares/array/A/examples/example/invalid1
  Range: [(22,0)-(24,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml#/declares/array/A/examples/example/invalid2
  Property: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml#/declares/array/A/examples/example/invalid2
  Range: [(25,0)-(29,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml
