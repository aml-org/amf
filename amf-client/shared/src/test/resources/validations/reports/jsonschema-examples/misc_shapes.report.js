Model: file://amf-client/shared/src/test/resources/validations/jsonschema/misc_shapes.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"required","dataPath":"","schemaPath":"#/required","params":{"missingProperty":"emailAddresses"},"message":"should have required property 'emailAddresses'"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/misc_shapes.raml#/web-api/end-points/%2Fep1/get/200/application%2Fjson/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(39,21)-(44,15)]))
  Location: 
