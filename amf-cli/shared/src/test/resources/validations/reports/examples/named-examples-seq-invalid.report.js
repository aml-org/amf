ModelId: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-seq-invalid/api.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [1].age should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-seq-invalid/api.raml#/declares/array/People/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-seq-invalid/api.raml#/declares/array/People/examples/example/default-example
  Range: [(2,0)-(5,11)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-seq-invalid/example.raml
