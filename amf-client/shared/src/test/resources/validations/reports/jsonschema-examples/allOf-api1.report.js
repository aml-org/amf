Model: file://amf-client/shared/src/test/resources/validations/jsonschema/allOf/api1.raml
Profile: RAML
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"required","dataPath":"","schemaPath":"#/allOf/0/required","params":{"missingProperty":"bar"},"message":"should have required property 'bar'"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(42,0)-(44,0)]))
  Location: 

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"required","dataPath":"","schemaPath":"#/allOf/1/required","params":{"missingProperty":"foo"},"message":"should have required property 'foo'"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep3/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(52,0)-(54,0)]))
  Location: 

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".bar","schemaPath":"#/allOf/0/properties/bar/type","params":{"type":"integer"},"message":"should be integer"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/allOf/api1.raml#/web-api/end-points/%2Fep4/get/200/application%2Fjson/any/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(62,0)-(63,23)]))
  Location: 
