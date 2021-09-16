ModelId: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-pattern-invalid.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match pattern "^([\u30a0-\u30ff]+)$"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-pattern-invalid.raml#/declarations/types/scalar/InvalidKatakanaEmail/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-pattern-invalid.raml#/declarations/types/scalar/InvalidKatakanaEmail/example/default-example
  Range: [(6,13)-(6,21)]
  Location: file://amf-cli/shared/src/test/resources/validations/japanese/raml/facet-string-pattern-invalid.raml
