ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/declares/shape/User/examples/example/bad
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/declares/shape/User/examples/example/bad
  Range: [(13,10)-(16,11)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/web-api/endpoint/%2Fsend/supportedOperation/post/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/web-api/endpoint/%2Fsend/supportedOperation/post/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(23,16)-(26,17)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml
