ModelId: file://amf-cli/shared/src/test/resources/validations/examples/nil_validation.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: middlename should be null
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/nil_validation.raml#/declares/shape/User/examples/example/wrong-type
  Property: file://amf-cli/shared/src/test/resources/validations/examples/nil_validation.raml#/declares/shape/User/examples/example/wrong-type
  Range: [(15,0)-(17,31)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/nil_validation.raml
