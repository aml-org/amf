ModelId: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-facet-no-key/api.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be object
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-facet-no-key/api.raml#/declares/shape/Person/examples/example/name
  Property: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-facet-no-key/api.raml#/declares/shape/Person/examples/example/name
  Range: [(2,6)-(2,10)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-facet-no-key/example.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be object
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-facet-no-key/api.raml#/declares/shape/Person/examples/example/age
  Property: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-facet-no-key/api.raml#/declares/shape/Person/examples/example/age
  Range: [(3,5)-(3,7)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/named-examples-facet-no-key/example.raml
