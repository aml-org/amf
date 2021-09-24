ModelId: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have additional properties
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml#/web-api/endpoint/%2Fendpoint1/supportedOperation/get/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml#/web-api/endpoint/%2Fendpoint1/supportedOperation/get/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(19,0)-(21,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml
