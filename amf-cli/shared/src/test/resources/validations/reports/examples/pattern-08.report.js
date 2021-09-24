ModelId: file://amf-cli/shared/src/test/resources/validations/08/pattern.raml
Profile: RAML 0.8
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match pattern "^[^0-9]*$"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/08/pattern.raml#/web-api/endpoint/%2Fresources/supportedOperation/get/expects/request/parameter/parameter/query/param/scalar/param/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/08/pattern.raml#/web-api/endpoint/%2Fresources/supportedOperation/get/expects/request/parameter/parameter/query/param/scalar/param/examples/example/default-example
  Range: [(11,21)-(11,24)]
  Location: file://amf-cli/shared/src/test/resources/validations/08/pattern.raml
