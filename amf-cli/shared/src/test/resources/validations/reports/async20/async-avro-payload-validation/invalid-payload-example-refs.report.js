ModelId: file://amf-cli/shared/src/test/resources/validations/async20/validations/async-avro-payload-validation/invalid-payload-example-refs.yaml
Profile: ASYNC 2.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: 'invalid string value' is not a valid value (of type '"int"') for 'simpleIntField'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/async-avro-payload-validation/invalid-payload-example-refs.yaml#/async-api/endpoint/first-channel/supportedOperation/subscribe/returns/resp/default-response/examples/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/async-avro-payload-validation/invalid-payload-example-refs.yaml#/async-api/endpoint/first-channel/supportedOperation/subscribe/returns/resp/default-response/examples/example/default-example_1
  Range: [(16,0)-(17,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/async-avro-payload-validation/invalid-payload-example-refs.yaml
