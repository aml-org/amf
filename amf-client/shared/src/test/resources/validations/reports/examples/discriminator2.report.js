Model: file://amf-client/shared/src/test/resources/validations/examples/discriminator2.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: phone should match pattern "^[0-9|-]+$"
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/discriminator2.raml#/web-api/end-points/%2Forgs%2F%7BorgId%7D/get/200/application%2Fjson/schema/examples/example/default-example
  Property: 
  Position: Some(LexicalInformation([(44,0)-(52,29)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/discriminator2.raml
