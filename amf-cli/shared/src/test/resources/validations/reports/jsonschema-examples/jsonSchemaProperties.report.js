ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: ['I_4'] should be integer
['S_0'] should be string
['a'] should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml#/web-api/endpoint/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml#/web-api/endpoint/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(28,0)-(34,27)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/jsonSchemaProperties.raml
