ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api3.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be valid
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api3.raml#/web-api/endpoint/%2Fep3/supportedOperation/get/returns/resp/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api3.raml#/web-api/endpoint/%2Fep3/supportedOperation/get/returns/resp/200/payload/application%2Fjson/any/schema/examples/example/default-example
  Range: [(44,0)-(44,22)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/not/api3.raml
