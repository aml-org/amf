Model: file://amf-client/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"format","dataPath":".startDate","schemaPath":"#/properties/startDate/format","params":{"format":"date"},"message":"should match format \"date\""}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml#/web-api/end-points/%2Fsubscription/post/request/application%2Fjson/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: 

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#parsing-warning
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(9,8)-(9,14)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml
