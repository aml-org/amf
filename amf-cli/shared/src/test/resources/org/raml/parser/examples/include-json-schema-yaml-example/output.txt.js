ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml#/declares/shape/User/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml#/declares/shape/User/examples/example/default-example
  Range: [(7,0)-(9,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml
