Model: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":"","schemaPath":"#/type","params":{"type":"string"},"message":"should be string"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(25,21)-(25,22)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"anyOf","dataPath":"","schemaPath":"#/anyOf","params":{},"message":"should match some schema in anyOf"}
{"keyword":"maxLength","dataPath":"","schemaPath":"#/anyOf/0/maxLength","params":{"limit":2},"message":"should NOT be longer than 2 characters"}
{"keyword":"minLength","dataPath":"","schemaPath":"#/anyOf/1/minLength","params":{"limit":4},"message":"should NOT be shorter than 4 characters"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/scalar/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/scalar/schema/example/default-example
  Position: Some(LexicalInformation([(43,21)-(43,24)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml
