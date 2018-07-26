Model: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api2.raml
Profile: RAML
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should NOT be valid
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api2.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(17,21)-(17,24)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api2.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should NOT be valid
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api2.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(26,21)-(26,22)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api2.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should NOT be valid
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api2.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(35,21)-(35,25)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/not/api2.raml
