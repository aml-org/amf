Model: file://amf-cli/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
id should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml/#/web-api/endpoint/end-points/%2Fresource/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml/#/web-api/endpoint/end-points/%2Fresource/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(27,0)-(29,25)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/multiple-inheritance-validation/input.raml
