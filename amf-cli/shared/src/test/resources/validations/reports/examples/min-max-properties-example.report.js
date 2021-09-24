ModelId: file://amf-cli/shared/src/test/resources/validations/examples/min-max-properties-example.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have fewer than 2 properties
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/min-max-properties-example.raml#/declares/shape/InvalidMax/examples/example/badMin
  Property: file://amf-cli/shared/src/test/resources/validations/examples/min-max-properties-example.raml#/declares/shape/InvalidMax/examples/example/badMin
  Range: [(15,0)-(16,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/min-max-properties-example.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 2 properties
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/min-max-properties-example.raml#/declares/shape/InvalidMax/examples/example/badMax
  Property: file://amf-cli/shared/src/test/resources/validations/examples/min-max-properties-example.raml#/declares/shape/InvalidMax/examples/example/badMax
  Range: [(17,0)-(20,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/min-max-properties-example.raml
