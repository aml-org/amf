Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: age should be integer
id should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml#/web-api/end-points/%2Fresource/get/200/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml#/web-api/end-points/%2Fresource/get/200/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(27,0)-(29,25)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml
