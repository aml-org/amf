Model: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have additional properties
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml/#/web-api/endpoint/end-points/%2Fendpoint1/supportedOperation/get/expects/request/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml/#/web-api/endpoint/end-points/%2Fendpoint1/supportedOperation/get/expects/request/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(19,0)-(21,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/discriminator/invalid/closed-parent.raml
