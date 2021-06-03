Model: file://amf-client/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-mediaType/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-mediaType/get/200/application%2Fjson/any/schema/example/default-example
  Position: Some(LexicalInformation([(25,21)-(25,22)]))
  Location: file://amf-client/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-no-mediaType/get/200/default/any/default/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml#/web-api/end-points/%2Fe-no-mediaType/get/200/default/any/default/example/default-example
  Position: Some(LexicalInformation([(32,19)-(32,20)]))
  Location: file://amf-client/shared/src/test/resources/validations/extends/merging-payloads/media-type-single-response/equallyDefined.raml
