ModelId: file://amf-cli/shared/src/test/resources/validations/examples/double-example-inttype.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/double-example-inttype.raml#/declares/shape/orders/property/property/Priority/scalar/Priority/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/double-example-inttype.raml#/declares/shape/orders/property/property/Priority/scalar/Priority/examples/example/default-example
  Range: [(9,17)-(9,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/double-example-inttype.raml
