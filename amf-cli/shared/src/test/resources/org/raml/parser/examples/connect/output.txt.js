Model: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: orders[1].items[0].quantity should be integer
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml/#/web-api/endpoint/end-points/%2Forders/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/multiple-orders
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml/#/web-api/endpoint/end-points/%2Forders/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/multiple-orders
  Position: Some(LexicalInformation([(42,0)-(59,37)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml
