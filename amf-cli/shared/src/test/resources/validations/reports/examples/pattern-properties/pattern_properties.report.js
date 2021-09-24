ModelId: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: ['note1'] should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties.raml#/web-api/endpoint/%2Ftest/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(18,0)-(19,65)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties.raml
