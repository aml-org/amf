Model: file://amf-client/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: orders[0].items[0].quantity should be integer
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml#/web-api/end-points/%2Forders/get/200/application%2Fjson/schema/example/single-order
  Property: file://amf-client/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml#/web-api/end-points/%2Forders/get/200/application%2Fjson/schema/example/single-order
  Position: Some(LexicalInformation([(16,0)-(27,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml
