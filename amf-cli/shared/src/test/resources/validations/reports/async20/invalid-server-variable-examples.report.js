ModelId: file://amf-cli/shared/src/test/resources/validations/async20/validations/invalid-server-variable-examples.yaml
Profile: ASYNC 2.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/invalid-server-variable-examples.yaml#/async-api/server/%7Busername%7D.gigantic-server.com%3A%7Bport%7D/variable/parameter/path/invalid/examples/example/example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/invalid-server-variable-examples.yaml#/async-api/server/%7Busername%7D.gigantic-server.com%3A%7Bport%7D/variable/parameter/path/invalid/examples/example/example_1
  Range: [(35,12)-(35,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/invalid-server-variable-examples.yaml
