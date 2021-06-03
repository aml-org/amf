Model: file://amf-client/shared/src/test/resources/validations/examples/pattern-invalid.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: signature should match pattern "^\d{3}-\w{12}$"
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/pattern-invalid.raml#/web-api/end-points/%2Fusers/clearanceLevel/object_1
  Property: file://amf-client/shared/src/test/resources/validations/examples/pattern-invalid.raml#/web-api/end-points/%2Fusers/clearanceLevel/object_1
  Position: Some(LexicalInformation([(15,0)-(16,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/pattern-invalid.raml
