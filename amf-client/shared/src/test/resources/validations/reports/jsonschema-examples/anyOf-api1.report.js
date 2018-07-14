Model: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"anyOf","dataPath":"","schemaPath":"#/anyOf","params":{},"message":"should match some schema in anyOf"}
{"keyword":"minimum","dataPath":"","schemaPath":"#/anyOf/1/minimum","params":{"comparison":">=","limit":2,"exclusive":false},"message":"should be >= 2"}
{"keyword":"type","dataPath":"","schemaPath":"#/anyOf/0/type","params":{"type":"integer"},"message":"should be integer"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Position: Some(LexicalInformation([(51,21)-(51,24)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml
