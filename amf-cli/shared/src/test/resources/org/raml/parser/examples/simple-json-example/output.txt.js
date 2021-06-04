Model: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/simple-json-example/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: lastName should NOT be shorter than 10 characters
name should NOT be longer than 5 characters

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/simple-json-example/input.raml#/declarations/types/User/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/simple-json-example/input.raml#/declarations/types/User/example/default-example
  Position: Some(LexicalInformation([(14,12)-(17,13)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/simple-json-example/input.raml
