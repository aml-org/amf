Model: file://amf-client/shared/src/test/resources/validations/examples/nil_validation.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: middlename should be null
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/nil_validation.raml#/declarations/types/User/example/wrong-type
  Property: file://amf-client/shared/src/test/resources/validations/examples/nil_validation.raml#/declarations/types/User/example/wrong-type
  Position: Some(LexicalInformation([(15,0)-(17,31)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/nil_validation.raml
