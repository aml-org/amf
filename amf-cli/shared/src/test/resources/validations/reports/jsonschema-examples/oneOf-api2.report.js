Model: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api2.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":"","schemaPath":"#/type","params":{"type":"string"},"message":"should be string"}
{"keyword":"oneOf","dataPath":"","schemaPath":"#/oneOf","params":{"passingSchemas":[0,1]},"message":"should match exactly one schema in oneOf"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api2.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/scalar/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api2.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/scalar/schema/example/default-example
  Position: Some(LexicalInformation([(25,21)-(25,22)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api2.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"oneOf","dataPath":"","schemaPath":"#/oneOf","params":{"passingSchemas":[0,1]},"message":"should match exactly one schema in oneOf"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/oneOf/api2.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(43,21)-(43,24)]))
  Location: 
