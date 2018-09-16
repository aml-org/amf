Model: file://amf-client/shared/src/test/resources/validations/examples/invalid-default.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should match pattern "^[a-z]*$"
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-default.raml#/declarations/types/scalar/name/scalar_1
  Property: 
  Position: Some(LexicalInformation([(7,13)-(7,20)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-default.raml
