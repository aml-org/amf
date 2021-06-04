Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: ['I_4'] should be integer
['S_0'] should be string
['a'] should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(28,0)-(34,27)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml
