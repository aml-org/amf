Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/multiple/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be object
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/multiple/input.raml#/web-api/end-points/%2Forganisation/post/request/application%2Fjson/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(30,17)-(30,23)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/multiple/input.raml

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be object
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/multiple/input.raml#/web-api/end-points/%2Forganisation/get/201/application%2Fjson/schema/example/softwareCorp
  Property: 
  Position: Some(LexicalInformation([(45,23)-(45,27)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/multiple/input.raml
