ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be longer than 2 characters
should NOT be shorter than 4 characters
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml#/web-api/endpoint/%2Fep3/supportedOperation/get/returns/resp/200/payload/application%2Fjson/scalar/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml#/web-api/endpoint/%2Fep3/supportedOperation/get/returns/resp/200/payload/application%2Fjson/scalar/schema/examples/example/default-example
  Range: [(43,21)-(43,24)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/anyOf/api2.raml
