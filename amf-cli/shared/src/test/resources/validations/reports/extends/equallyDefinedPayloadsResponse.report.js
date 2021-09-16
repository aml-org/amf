ModelId: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-mediaType/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-mediaType/get/200/application%2Fjson/any/schema/example/default-example
  Range: [(25,21)-(25,22)]
  Location: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-no-mediaType/get/200/default/any/default/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-no-mediaType/get/200/default/any/default/example/default-example
  Range: [(32,19)-(32,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml
