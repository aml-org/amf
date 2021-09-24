ModelId: file://amf-cli/shared/src/test/resources/validations/examples/union1-invalid.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
should be null
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/union1-invalid.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/expects/request/header/parameter/header/date/union/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/union1-invalid.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/expects/request/header/parameter/header/date/union/schema/examples/example/default-example
  Range: [(10,17)-(10,21)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/union1-invalid.raml
