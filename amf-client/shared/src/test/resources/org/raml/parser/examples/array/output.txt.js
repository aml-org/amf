Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/array/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [1].active should be boolean
[1].address should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/array/input.raml#/declarations/types/array/Emails/example/default-example
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/examples/array/input.raml#/declarations/types/array/Emails/example/default-example
  Position: Some(LexicalInformation([(11,0)-(14,25)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/array/input.raml
