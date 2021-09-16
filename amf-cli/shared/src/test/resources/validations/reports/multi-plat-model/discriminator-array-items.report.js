ModelId: file://amf-cli/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [0] should have required property 'anotherProp'
[1] should NOT have additional properties

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml#/web-api/end-points/%2Finvalid/put/request/array/default/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml#/web-api/end-points/%2Finvalid/put/request/array/default/example/default-example
  Range: [(39,0)-(42,58)]
  Location: file://amf-cli/shared/src/test/resources/validations/discriminator/discriminator-array-items.raml
