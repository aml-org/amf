Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: nodes[0].subtree.nodes[0].value should be number
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml#/web-api/endpoint/end-points/%2Fep2/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml#/web-api/endpoint/end-points/%2Fep2/supportedOperation/get/returns/200/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(61,0)-(75,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml
