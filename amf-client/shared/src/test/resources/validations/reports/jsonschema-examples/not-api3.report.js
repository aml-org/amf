Model: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api3.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should NOT be valid
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api3.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/examples/example/default-example
  Property: 
  Position: Some(LexicalInformation([(44,0)-(44,22)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api3.raml
