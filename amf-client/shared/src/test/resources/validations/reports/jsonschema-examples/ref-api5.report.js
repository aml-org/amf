Model: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api5.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: foo should be array
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api5.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/schema/examples/example/default-example
  Property: 
  Position: Some(LexicalInformation([(48,0)-(48,27)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api5.raml
