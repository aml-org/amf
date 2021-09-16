ModelId: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties2.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: ['note1'] should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties2.raml#/web-api/end-points/%2Ftest/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties2.raml#/web-api/end-points/%2Ftest/get/200/application%2Fjson/schema/example/default-example
  Range: [(17,0)-(18,65)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties2.raml
