Model: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api5.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".foo","schemaPath":"#/properties/foo/type","params":{"type":"array"},"message":"should be array"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api5.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(48,0)-(48,27)]))
  Location: 
