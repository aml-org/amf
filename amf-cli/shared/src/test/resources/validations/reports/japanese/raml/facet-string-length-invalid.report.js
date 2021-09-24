ModelId: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-length-invalid.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be longer than 7 characters
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-length-invalid.raml#/declares/scalar/TooLongEmail/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-length-invalid.raml#/declares/scalar/TooLongEmail/examples/example/default-example
  Range: [(6,13)-(6,21)]
  Location: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-length-invalid.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be shorter than 10 characters
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-length-invalid.raml#/declares/scalar/TooShortEmail/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-length-invalid.raml#/declares/scalar/TooShortEmail/examples/example/default-example
  Range: [(10,13)-(10,21)]
  Location: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-length-invalid.raml
