Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have additional properties
should have required property 'wadus'

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(21,0)-(23,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: left should NOT have additional properties
left should have required property 'wadus'

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(31,0)-(35,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/ref/api7.raml
