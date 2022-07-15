ModelId: file://amf-cli/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml#/declares/scalar/unionType/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml#/declares/scalar/unionType/example/default-example
  Range: [(16,13)-(16,16)]
  Location: file://amf-cli/shared/src/test/resources/validations/raml/invalid-example-in-union-with-members-with-enums.raml