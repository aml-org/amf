Model: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"oneOf","dataPath":"","schemaPath":"#/oneOf","params":{"passingSchemas":[0,1]},"message":"should match exactly one schema in oneOf"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(42,21)-(42,22)]))
  Location: 

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"minimum","dataPath":"","schemaPath":"#/oneOf/1/minimum","params":{"comparison":">=","limit":2,"exclusive":false},"message":"should be >= 2"}
{"keyword":"oneOf","dataPath":"","schemaPath":"#/oneOf","params":{"passingSchemas":null},"message":"should match exactly one schema in oneOf"}
{"keyword":"type","dataPath":"","schemaPath":"#/oneOf/0/type","params":{"type":"integer"},"message":"should be integer"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Position: Some(LexicalInformation([(51,21)-(51,24)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml
