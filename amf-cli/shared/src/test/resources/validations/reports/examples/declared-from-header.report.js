ModelId: file://amf-cli/shared/src/test/resources/validations/examples/declared-from-header.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be longer than 2 characters
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/declared-from-header.raml#/web-api/endpoint/%2Fcustomers/supportedOperation/get/expects/request/header/parameter/header/newHeader/scalar/schema/inherits/scalar/person/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/declared-from-header.raml#/web-api/endpoint/%2Fcustomers/supportedOperation/get/expects/request/header/parameter/header/newHeader/scalar/schema/inherits/scalar/person/examples/example/default-example
  Range: [(8,13)-(8,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/declared-from-header.raml
