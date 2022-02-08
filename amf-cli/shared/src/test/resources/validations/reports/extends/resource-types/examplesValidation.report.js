ModelId: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml#/web-api/endpoint/%2FnonParametrizedIncorrect/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml#/web-api/endpoint/%2FnonParametrizedIncorrect/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Range: [(34,17)-(34,19)]
  Location: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml
