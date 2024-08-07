ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have additional properties
should have required property 'wadus'

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/endpoint/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/endpoint/%2Fep1/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(21,0)-(23,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: left should NOT have additional properties
left should have required property 'wadus'

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/endpoint/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/endpoint/%2Fep2/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(31,0)-(35,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml
