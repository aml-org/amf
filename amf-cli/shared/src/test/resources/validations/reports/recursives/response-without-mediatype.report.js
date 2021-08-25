Model: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: prop should be array
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml#/web-api/endpoint/%2Fendpoint/supportedOperation/get/returns/resp/200/payload/default/shape/default/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml#/web-api/endpoint/%2Fendpoint/supportedOperation/get/returns/resp/200/payload/default/shape/default/examples/example/default-example
  Position: Some(LexicalInformation([(24,18)-(27,19)]))
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml
