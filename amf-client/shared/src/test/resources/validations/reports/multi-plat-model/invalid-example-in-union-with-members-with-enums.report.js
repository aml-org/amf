Model: file://amf-client/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
should match some schema in anyOf

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml#/declarations/types/scalar/unionType/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml#/declarations/types/scalar/unionType/example/default-example
  Position:  Some(LexicalInformation([(16,13)-(16,16)]))
  Location: file://amf-client/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml