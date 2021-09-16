ModelId: file://amf-cli/shared/src/test/resources/validations/async20/validations/message-payload-invalid-example.yaml
Profile: ASYNC 2.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/message-payload-invalid-example.yaml#/async-api/end-points/smartylighting%2Fstreetlights%2F1%2F0%2Fevent%2F%7BstreetlightId%7D%2Flighting%2Fmeasured/publish/request/example/default-example_2
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/message-payload-invalid-example.yaml#/async-api/end-points/smartylighting%2Fstreetlights%2F1%2F0%2Fevent%2F%7BstreetlightId%7D%2Flighting%2Fmeasured/publish/request/example/default-example_2
  Range: [(23,0)-(24,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/message-payload-invalid-example.yaml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/message-payload-invalid-example.yaml#/async-api/end-points/smartylighting%2Fstreetlights%2F1%2F0%2Fevent%2F%7BstreetlightId%7D%2Flighting%2Fmeasured/publish/request/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/message-payload-invalid-example.yaml#/async-api/end-points/smartylighting%2Fstreetlights%2F1%2F0%2Fevent%2F%7BstreetlightId%7D%2Flighting%2Fmeasured/publish/request/example/default-example_1
  Range: [(25,0)-(27,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/message-payload-invalid-example.yaml
