ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: nodes[0].subtree.nodes[0].value should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml#/web-api/endpoint/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml#/web-api/endpoint/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(61,0)-(75,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api6.raml
