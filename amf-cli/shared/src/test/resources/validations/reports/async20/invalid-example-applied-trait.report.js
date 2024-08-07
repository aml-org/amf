ModelId: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml
Profile: ASYNC 2.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_2
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_2
  Range: [(12,0)-(13,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml#/async-api/endpoint/%2Fuser%2Fsignedup/supportedOperation/subscribe/returns/resp/default-response/example/default-example_1
  Range: [(14,0)-(16,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/applied-message-trait-invalid-example.yaml
