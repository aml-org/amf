Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/schema-dependencies.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: restrictedProperty should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/schema-dependencies.raml#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/schema-dependencies.raml#/web-api/endpoint/end-points/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Position: Some(LexicalInformation([(32,0)-(35,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/dependencies/schema-dependencies.raml
