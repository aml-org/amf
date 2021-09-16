ModelId: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: prop should be array
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml#/web-api/end-points/%2Fendpoint/get/200/default/default/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml#/web-api/end-points/%2Fendpoint/get/200/default/default/example/default-example
  Range: [(24,18)-(27,19)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/response-without-mediatype.raml
