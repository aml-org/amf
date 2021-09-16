ModelId: file://amf-cli/shared/src/test/resources/validations/examples/named-example-facet-other/api.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'age'
should have required property 'name'

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/named-example-facet-other/api.raml#/declarations/types/Person/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/named-example-facet-other/api.raml#/declarations/types/Person/example/default-example
  Range: [(2,0)-(4,9)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/named-example-facet-other/example.raml
