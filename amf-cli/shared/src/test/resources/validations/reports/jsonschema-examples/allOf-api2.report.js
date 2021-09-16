ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml
Profile: RAML 1.0
Conforms: false
Number of results: 4

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'bar'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/schema/example/default-example
  Range: [(45,0)-(49,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'foo'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/schema/example/default-example
  Range: [(57,0)-(61,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'baz'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/schema/example/default-example
  Range: [(69,0)-(72,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'baz'
should have required property 'foo'

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep5/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml#/web-api/end-points/%2Fep5/get/200/application%2Fjson/schema/example/default-example
  Range: [(80,0)-(80,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api2.raml
