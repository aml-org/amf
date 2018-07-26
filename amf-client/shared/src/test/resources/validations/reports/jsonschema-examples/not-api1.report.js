Model: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api1.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should NOT be valid
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api1.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(26,21)-(26,22)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api1.raml
