Model: file://amf-client/shared/src/test/resources/validations/examples/number-prop-bool-example.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".prop2","schemaPath":"#/properties/prop2/type","params":{"type":"number"},"message":"should be number"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/number-prop-bool-example.raml#/web-api/end-points/%2Fteams/get/request/application%2Fxml/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(16,0)-(20,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/number-prop-bool-example.raml
