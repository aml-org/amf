Model: file://amf-client/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".orders[0].items[0].quantity","schemaPath":"#/properties/orders/items/properties/items/items/properties/quantity/type","params":{"type":"integer"},"message":"should be integer"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml#/web-api/end-points/%2Forders/get/200/application%2Fjson/schema/example/single-order
  Property: 
  Position: Some(LexicalInformation([(16,0)-(27,0)]))
  Location: 
