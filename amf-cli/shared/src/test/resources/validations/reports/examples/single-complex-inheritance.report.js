Model: file://amf-client/shared/src/test/resources/validations/examples/single-complex-inheritance.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 3 properties
should have required property 'numberOfUSBPorts'
should match some schema in anyOf

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/single-complex-inheritance.raml#/web-api/end-points/%2Ftest/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/single-complex-inheritance.raml#/web-api/end-points/%2Ftest/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(33,0)-(36,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/single-complex-inheritance.raml
