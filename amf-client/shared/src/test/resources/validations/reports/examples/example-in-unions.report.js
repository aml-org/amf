Model: file://amf-client/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"anyOf","dataPath":"","schemaPath":"#/anyOf","params":{},"message":"should match some schema in anyOf"}
{"keyword":"type","dataPath":"","schemaPath":"#/anyOf/0/type","params":{"type":"string"},"message":"should be string"}
{"keyword":"type","dataPath":"","schemaPath":"#/anyOf/1/type","params":{"type":"integer"},"message":"should be integer"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml#/shape/example/ex3
  Property: file://amf-client/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml#/shape/example/ex3
  Position: Some(LexicalInformation([(8,0)-(9,22)]))
  Location: file://amf-client/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml
