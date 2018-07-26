Model: file://amf-client/shared/src/test/resources/validations/08/validation_error1.raml
Profile: RAML08
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should be object
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/08/validation_error1.raml#/web-api/end-points/%2Freservations%2F%7Bpnrcreationdate%7D/get/200/application%2Fjson/any/application%2Fjson/example/default-example
  Property: 
  Position: Some(LexicalInformation([(26,27)-(29,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/08/validation_error1.raml
