ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: Priority should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml#/web-api/endpoint/%2Fresource/supportedOperation/post/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml#/web-api/endpoint/%2Fresource/supportedOperation/post/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(15,0)-(16,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml
