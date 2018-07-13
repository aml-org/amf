Model: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api3.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"oneOf","dataPath":"","schemaPath":"#/oneOf","params":{"passingSchemas":[0,1]},"message":"should match exactly one schema in oneOf"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api3.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(51,0)-(54,0)]))
  Location: 

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".bar","schemaPath":"#/oneOf/0/properties/bar/type","params":{"type":"integer"},"message":"should be integer"}
{"keyword":"type","dataPath":".foo","schemaPath":"#/oneOf/1/properties/foo/type","params":{"type":"string"},"message":"should be string"}
{"keyword":"oneOf","dataPath":"","schemaPath":"#/oneOf","params":{"passingSchemas":null},"message":"should match exactly one schema in oneOf"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api3.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api3.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Position: Some(LexicalInformation([(62,0)-(63,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api3.raml
