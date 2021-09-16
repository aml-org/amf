ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'bar'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/any/schema/example/default-example
  Range: [(42,0)-(44,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'foo'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Range: [(52,0)-(54,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: bar should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Range: [(62,0)-(63,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api1.raml
