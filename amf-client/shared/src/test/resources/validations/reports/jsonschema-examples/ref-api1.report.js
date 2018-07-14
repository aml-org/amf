Model: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api1.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"additionalProperties","dataPath":".foo","schemaPath":"#/properties/foo/anyOf/0/additionalProperties","params":{"additionalProperty":"bar"},"message":"should NOT have additional properties"}
{"keyword":"anyOf","dataPath":".foo","schemaPath":"#/properties/foo/anyOf","params":{},"message":"should match some schema in anyOf"}
{"keyword":"type","dataPath":".foo","schemaPath":"#/properties/foo/anyOf/1/type","params":{"type":"boolean"},"message":"should be boolean"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(48,0)-(51,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api1.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"anyOf","dataPath":".foo","schemaPath":"#/properties/foo/anyOf","params":{},"message":"should match some schema in anyOf"}
{"keyword":"anyOf","dataPath":".foo.foo","schemaPath":"#/properties/foo/anyOf/0/properties/foo/anyOf","params":{},"message":"should match some schema in anyOf"}
{"keyword":"type","dataPath":".foo","schemaPath":"#/properties/foo/anyOf/1/type","params":{"type":"boolean"},"message":"should be boolean"}
{"keyword":"type","dataPath":".foo.foo","schemaPath":"#/properties/foo/anyOf/0/properties/foo/anyOf/1/type","params":{"type":"boolean"},"message":"should be boolean"}
{"keyword":"type","dataPath":".foo.foo","schemaPath":"#/type","params":{"type":"object"},"message":"should be object"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(59,0)-(60,27)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api1.raml
