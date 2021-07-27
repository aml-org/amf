Model: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be string
b.b should be string
c should be object
c should be string
should match some schema in anyOf

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml/declares/any/A/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml/declares/any/A/example/invalid
  Position: Some(LexicalInformation([(12,0)-(15,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/union1.raml
