Model: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".lastName","schemaPath":"#/properties/lastName/type","params":{"type":"string"},"message":"should be string"}
{"keyword":"type","dataPath":".name","schemaPath":"#/properties/name/type","params":{"type":"string"},"message":"should be string"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml#/web-api/end-points/%2Fendpoint/get/request/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml#/web-api/end-points/%2Fendpoint/get/request/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(16,0)-(18,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".lastName","schemaPath":"#/properties/lastName/type","params":{"type":"string"},"message":"should be string"}
{"keyword":"type","dataPath":".name","schemaPath":"#/properties/name/type","params":{"type":"string"},"message":"should be string"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml#/web-api/end-points/%2Fendpoint/post/request/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml#/web-api/end-points/%2Fendpoint/post/request/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(23,0)-(25,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml
