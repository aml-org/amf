Model: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have additional properties
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml#/declares/Animal/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml#/declares/Animal/examples/example/default-example
  Position: Some(LexicalInformation([(18,0)-(20,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/additional-properties/input.raml
