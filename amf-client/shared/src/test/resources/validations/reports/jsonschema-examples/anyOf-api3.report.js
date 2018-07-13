Model: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".bar","schemaPath":"#/anyOf/0/properties/bar/type","params":{"type":"integer"},"message":"should be integer"}
{"keyword":"type","dataPath":".foo","schemaPath":"#/anyOf/1/properties/foo/type","params":{"type":"string"},"message":"should be string"}
{"keyword":"anyOf","dataPath":"","schemaPath":"#/anyOf","params":{},"message":"should match some schema in anyOf"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Position: Some(LexicalInformation([(62,0)-(63,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml
