ModelId: file://amf-cli/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: orders[0].items[0].quantity should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml#/web-api/endpoint/%2Forders/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/single-order
  Property: file://amf-cli/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml#/web-api/endpoint/%2Forders/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/single-order
  Range: [(16,0)-(27,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/invalid-property-in-array-items.raml
