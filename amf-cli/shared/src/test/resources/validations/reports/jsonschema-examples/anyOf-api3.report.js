Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: bar should be integer
foo should be string
should match some schema in anyOf

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml#/web-api/endpoint/end-points/%2Fep4/supportedOperation/get/returns/resp/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml#/web-api/endpoint/end-points/%2Fep4/supportedOperation/get/returns/resp/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Position: Some(LexicalInformation([(62,0)-(63,23)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api3.raml
