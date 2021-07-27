Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api5.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: foo should be array
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api5.raml/#/web-api/endpoint/end-points/%2Fep3/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api5.raml/#/web-api/endpoint/end-points/%2Fep3/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(48,0)-(48,27)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api5.raml
