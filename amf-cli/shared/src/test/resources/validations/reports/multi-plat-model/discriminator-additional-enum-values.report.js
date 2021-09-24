ModelId: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: enumProp should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml#/web-api/endpoint/%2Fendpoint1/supportedOperation/get/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml#/web-api/endpoint/%2Fendpoint1/supportedOperation/get/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(26,0)-(28,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml
