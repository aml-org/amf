Model: file://amf-cli/shared/src/test/resources/validations/08/date-query-parameter.raml
Profile: RAML 0.8
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "RFC2616"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/08/date-query-parameter.raml#/web-api/endpoint/%2Ftickets/supportedOperation/get/expects/request/parameter/parameter/query/createdAfter/scalar/createdAfter/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/08/date-query-parameter.raml#/web-api/endpoint/%2Ftickets/supportedOperation/get/expects/request/parameter/parameter/query/createdAfter/scalar/createdAfter/examples/example/default-example
  Position: Some(LexicalInformation([(16,17)-(16,36)]))
  Location: file://amf-cli/shared/src/test/resources/validations/08/date-query-parameter.raml
