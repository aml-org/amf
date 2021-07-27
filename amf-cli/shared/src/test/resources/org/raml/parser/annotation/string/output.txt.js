Model: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml/#/web-api/endpoint/end-points/%2Ftext/customDomainProperties/extension/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml/#/web-api/endpoint/end-points/%2Ftext/customDomainProperties/extension/scalar_1
  Position: Some(LexicalInformation([(18,11)-(18,12)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be longer than 2 characters
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml/#/web-api/endpoint/end-points/%2Ftext/customDomainProperties/extension_2/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml/#/web-api/endpoint/end-points/%2Ftext/customDomainProperties/extension_2/scalar_1
  Position: Some(LexicalInformation([(19,9)-(19,12)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be shorter than 10 characters
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml/#/web-api/endpoint/end-points/%2Ftext/customDomainProperties/extension_3/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml/#/web-api/endpoint/end-points/%2Ftext/customDomainProperties/extension_3/scalar_1
  Position: Some(LexicalInformation([(20,10)-(20,15)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml
