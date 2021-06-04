Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be >= 2
should be integer
should match some schema in anyOf

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Position: Some(LexicalInformation([(51,21)-(51,24)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api1.raml
