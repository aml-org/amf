ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: orders[1].items[0].quantity should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml#/web-api/endpoint/%2Forders/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/multiple-orders
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml#/web-api/endpoint/%2Forders/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/multiple-orders
  Range: [(42,0)-(59,37)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/connect/input.raml
