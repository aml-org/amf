ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api3.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 30
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api3.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/any/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api3.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/any/schema/example/default-example
  Range: [(29,21)-(29,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/allOf/api3.raml
