ModelId: file://amf-cli/shared/src/test/resources/validations/raml/union-with-props.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 2 properties
should have required property 'b2'
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/union-with-props.raml#/web-api/endpoint/%2Fmessages/supportedOperation/post/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/raml/union-with-props.raml#/web-api/endpoint/%2Fmessages/supportedOperation/post/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(25,0)-(27,39)]
  Location: file://amf-cli/shared/src/test/resources/validations/raml/union-with-props.raml
