Model: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml#/web-api/endpoint/%2FnonParametrizedIncorrect/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml#/web-api/endpoint/%2FnonParametrizedIncorrect/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Position: Some(LexicalInformation([(34,17)-(34,19)]))
  Location: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml#/web-api/endpoint/%2FparametrizedIncorrect/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml#/web-api/endpoint/%2FparametrizedIncorrect/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Position: Some(LexicalInformation([(52,17)-(52,19)]))
  Location: file://amf-cli/shared/src/test/resources/validations/resource_types/examplesValidation.raml
