Model: file://amf-client/shared/src/test/resources/validations/jsonschema/array_unions.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: Values[2] should have required property 'b'
Values[2] should match some schema in anyOf
Values[2].a should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/array_unions.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/jsonschema/array_unions.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(50,20)-(57,21)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/array_unions.raml
