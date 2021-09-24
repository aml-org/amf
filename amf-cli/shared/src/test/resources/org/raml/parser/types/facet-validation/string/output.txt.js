ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be shorter than 10 characters
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml#/declares/scalar/DefaultMaxLength/examples/example/two
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml#/declares/scalar/DefaultMaxLength/examples/example/two
  Range: [(14,13)-(14,18)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml
