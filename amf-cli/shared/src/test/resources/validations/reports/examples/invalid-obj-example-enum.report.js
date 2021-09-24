ModelId: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml#/declares/shape/A/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml#/declares/shape/A/examples/example/default-example
  Range: [(14,0)-(15,16)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/invalid-obj-example-enum.raml
