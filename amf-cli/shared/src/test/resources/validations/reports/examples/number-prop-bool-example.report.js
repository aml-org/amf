ModelId: file://amf-cli/shared/src/test/resources/validations/examples/number-prop-bool-example.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: prop2 should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/number-prop-bool-example.raml#/web-api/endpoint/%2Fteams/supportedOperation/get/expects/request/payload/application%2Fxml/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/number-prop-bool-example.raml#/web-api/endpoint/%2Fteams/supportedOperation/get/expects/request/payload/application%2Fxml/shape/schema/examples/example/default-example
  Range: [(16,0)-(20,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/number-prop-bool-example.raml
