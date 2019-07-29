Model: file://amf-client/shared/src/test/resources/validations/production/rest_connect/apibad.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be null
should be string
should match some schema in anyOf

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/production/rest_connect/apibad.raml#/web-api/end-points/%2Ftest/get/rest-connect.ignored/scalar_1
  Property: file://amf-client/shared/src/test/resources/validations/production/rest_connect/apibad.raml#/web-api/end-points/%2Ftest/get/rest-connect.ignored/scalar_1
  Position: Some(LexicalInformation([(26,28)-(26,32)]))
  Location: file://amf-client/shared/src/test/resources/validations/production/rest_connect/apibad.raml
