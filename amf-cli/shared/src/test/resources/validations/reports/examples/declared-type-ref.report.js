ModelId: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: lastName should be string
name should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref.raml#/declares/shape/Person/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref.raml#/declares/shape/Person/examples/example/default-example
  Range: [(10,0)-(13,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref.raml
