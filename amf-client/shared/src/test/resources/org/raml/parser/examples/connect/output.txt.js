Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/connect/input.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/examples/connect/assets-lib.raml#/declarations/types/Orders_validation
  Message: Object at / must be valid
Array items at //orders must be valid
Object at //orders/items must be valid
Array items at //orders/items/items must be valid
Object at //orders/items/items/items must be valid
Scalar at //orders/items/items/items/quantity must have data type http://www.w3.org/2001/XMLSchema#integer

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/connect/input.raml#/web-api/end-points/%2Forders/get/200/application%2Fjson/schema/example/multiple-orders
  Property: http://a.ml/vocabularies/data#orders
  Position: Some(LexicalInformation([(42,0)-(59,37)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/connect/input.raml
