ModelId: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/endpoint/%2Fe-mediaType/supportedOperation/get/returns/resp/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/endpoint/%2Fe-mediaType/supportedOperation/get/returns/resp/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Range: [(25,21)-(25,22)]
  Location: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/endpoint/%2Fe-no-mediaType/supportedOperation/get/returns/resp/200/payload/default/any/default/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/endpoint/%2Fe-no-mediaType/supportedOperation/get/returns/resp/200/payload/default/any/default/examples/example/default-example
  Range: [(32,19)-(32,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml
