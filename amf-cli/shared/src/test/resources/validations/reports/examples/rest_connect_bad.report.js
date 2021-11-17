ModelId: file://amf-cli/shared/src/test/resources/validations/production/rest_connect/apibad.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be null
should be string
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/production/rest_connect/apibad.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/customDomainProperties/ignored/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/production/rest_connect/apibad.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/customDomainProperties/ignored/scalar_1
  Range: [(26,28)-(26,32)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/rest_connect/apibad.raml
