Model: file://amf-client/shared/src/test/resources/validations/08/date-query-parameter.raml
Profile: RAML 0.8
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "RFC2616"
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/08/date-query-parameter.raml#/web-api/end-points/%2Ftickets/get/request/parameter/default-binding/createdAfter/scalar/createdAfter/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/08/date-query-parameter.raml#/web-api/end-points/%2Ftickets/get/request/parameter/default-binding/createdAfter/scalar/createdAfter/example/default-example
  Position: Some(LexicalInformation([(16,17)-(16,36)]))
  Location: file://amf-client/shared/src/test/resources/validations/08/date-query-parameter.raml
