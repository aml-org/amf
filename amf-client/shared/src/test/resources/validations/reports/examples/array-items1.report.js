Model: file://amf-client/shared/src/test/resources/validations/examples/arrayItems1.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"minItems","dataPath":".recipients","schemaPath":"#/properties/recipients/minItems","params":{"limit":1},"message":"should NOT have less than 1 items"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/arrayItems1.raml#/web-api/end-points/%2Fnotifications%2Femails/post/request/application%2Fjson/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(53,17)-(63,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/arrayItems1.raml
