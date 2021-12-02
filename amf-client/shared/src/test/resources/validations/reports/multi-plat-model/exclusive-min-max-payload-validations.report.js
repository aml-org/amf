Model: file://amf-client/shared/src/test/resources/validations/api-referencing-draft-6-7.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: numberMessage should be < 99
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/api-referencing-draft-6-7.raml#/web-api/end-points/%2Fdraft-6-numeric-exclusive-min/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/api-referencing-draft-6-7.raml#/web-api/end-points/%2Fdraft-6-numeric-exclusive-min/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(14,0)-(17,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/api-referencing-draft-6-7.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: numberMessage should be > 1
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/api-referencing-draft-6-7.raml#/web-api/end-points/%2Fdraft-7-numeric-exclusive-min/get/200/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/api-referencing-draft-6-7.raml#/web-api/end-points/%2Fdraft-7-numeric-exclusive-min/get/200/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(25,0)-(26,30)]))
  Location: file://amf-client/shared/src/test/resources/validations/api-referencing-draft-6-7.raml
