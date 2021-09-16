ModelId: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: enumProp should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml#/web-api/end-points/%2Fendpoint1/get/request/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml#/web-api/end-points/%2Fendpoint1/get/request/application%2Fjson/schema/example/default-example
  Range: [(26,0)-(28,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/additional-enum-values.raml
