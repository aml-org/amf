Model: file://amf-client/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":"['I_4']","schemaPath":"#/patternProperties/%5EI_/type","params":{"type":"integer"},"message":"should be integer"}
{"keyword":"type","dataPath":"['S_0']","schemaPath":"#/patternProperties/%5ES_/type","params":{"type":"string"},"message":"should be string"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(28,0)-(34,27)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml
