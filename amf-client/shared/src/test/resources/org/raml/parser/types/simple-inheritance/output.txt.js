Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/simple-inheritance/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: age should be >= 0
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/simple-inheritance/input.raml#/declarations/types/Office/examples/example/default-example
  Property: 
  Position: Some(LexicalInformation([(16,12)-(20,13)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/simple-inheritance/input.raml
