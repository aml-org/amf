ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api4.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api4.raml#/web-api/endpoint/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/scalar/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api4.raml#/web-api/endpoint/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/scalar/schema/examples/example/default-example
  Range: [(31,21)-(31,22)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api4.raml
