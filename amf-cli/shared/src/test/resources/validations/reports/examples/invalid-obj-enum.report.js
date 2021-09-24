ModelId: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-enum.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'value1'
should have required property 'value2'

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-enum.raml#/declares/shape/A/in/object_1
  Property: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-enum.raml#/declares/shape/A/in/object_1
  Range: [(9,8)-(9,13)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-enum.raml
