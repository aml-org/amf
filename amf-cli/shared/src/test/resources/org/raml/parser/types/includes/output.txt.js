Model: file://amf-cli/shared/src/test/resources/org/raml/parser/types/includes/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: chapters[0].name should NOT be longer than 10 characters
chapters[3].content should be string
chapters[3].name should be string
name should NOT be longer than 10 characters

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/includes/input.raml/references/0/type/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/includes/input.raml/references/0/type/examples/example/default-example
  Position: Some(LexicalInformation([(14,0)-(23,16)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/includes/book.raml
