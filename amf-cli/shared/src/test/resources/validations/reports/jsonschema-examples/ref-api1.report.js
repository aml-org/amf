ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api1.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: foo should NOT have additional properties
foo should be boolean
foo should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/endpoint/%2Fep3/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/endpoint/%2Fep3/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(48,0)-(51,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api1.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: foo should be boolean
foo should match some schema in anyOf
foo.foo should be boolean
foo.foo should be object
foo.foo should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/endpoint/%2Fep4/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api1.raml#/web-api/endpoint/%2Fep4/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/schema/examples/example/default-example
  Range: [(59,0)-(60,27)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api1.raml
