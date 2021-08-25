Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/array_unions.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: Values[2] should have required property 'b'
Values[2] should match some schema in anyOf
Values[2].a should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/array_unions.raml#/web-api/endpoint/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/array_unions.raml#/web-api/endpoint/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Position: Some(LexicalInformation([(50,20)-(57,21)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/array_unions.raml
