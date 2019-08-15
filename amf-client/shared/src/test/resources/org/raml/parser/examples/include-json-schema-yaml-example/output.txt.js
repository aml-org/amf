Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml#/declarations/types/User/example/default-example
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml#/declarations/types/User/example/default-example
  Position: Some(LexicalInformation([(7,0)-(9,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml
