Model: file://amf-client/shared/src/test/resources/validations/08/date-query-parameter.raml
Profile: RAML08
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"format","dataPath":"","schemaPath":"#/format","params":{"format":"RFC2616"},"message":"should match format \"RFC2616\""}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/08/date-query-parameter.raml#/web-api/end-points/%2Ftickets/get/request/parameter/createdAfter/scalar/createdAfter/example/default-example
  Property: 
  Position: Some(LexicalInformation([(16,17)-(16,36)]))
  Location: 
