Model: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties3.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":"['note1']","schemaPath":"#/patternProperties/%5E.*%24/type","params":{"type":"integer"},"message":"should be integer"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties3.raml#/web-api/end-points/%2Ftest/get/200/application%2Fjson/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(18,0)-(20,65)]))
  Location: 
