ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match exactly one schema in oneOf
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Range: [(42,21)-(42,22)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be >= 2
should be integer
should match exactly one schema in oneOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Range: [(51,21)-(51,24)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/oneOf/api1.raml
