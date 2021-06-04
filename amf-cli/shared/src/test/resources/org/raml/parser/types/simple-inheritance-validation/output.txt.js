Model: file://amf-cli/shared/src/test/resources/org/raml/parser/types/simple-inheritance-validation/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: id should be integer
name should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/simple-inheritance-validation/input.raml#/web-api/end-points/%2Fresource/get/200/application%2Fjson/schema/example/bad
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/simple-inheritance-validation/input.raml#/web-api/end-points/%2Fresource/get/200/application%2Fjson/schema/example/bad
  Position: Some(LexicalInformation([(26,0)-(27,24)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/simple-inheritance-validation/input.raml
