Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/strict/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/strict/input.raml#/declarations/types/scalar/two/examples/example/default-example
  Property: 
  Position: Some(LexicalInformation([(14,19)-(14,21)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/strict/input.raml

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/strict/input.raml#/declarations/types/scalar/three/examples/example/default-example
  Property: 
  Position: Some(LexicalInformation([(17,17)-(17,19)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/strict/input.raml
