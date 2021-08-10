Model: file://amf-cli/shared/src/test/resources/validations/types/lengths.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: lastname should NOT be longer than 5 characters
name should NOT be shorter than 5 characters

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/types/lengths.raml#/declares/shape/User/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/types/lengths.raml#/declares/shape/User/examples/example/default-example
  Position: Some(LexicalInformation([(15,0)-(16,26)]))
  Location: file://amf-cli/shared/src/test/resources/validations/types/lengths.raml
