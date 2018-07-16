Model: file://amf-client/shared/src/test/resources/validations/annotations/annotations_enum.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"enum","dataPath":".items","schemaPath":"#/properties/items/enum","params":{"allowedValues":["W","A"]},"message":"should be equal to one of the allowed values"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/test/object_1
  Property: 
  Position: Some(LexicalInformation([(23,0)-(25,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/annotations/annotations_enum.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"enum","dataPath":".items","schemaPath":"#/properties/items/enum","params":{"allowedValues":[2,3]},"message":"should be equal to one of the allowed values"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/testInt/object_1
  Property: 
  Position: Some(LexicalInformation([(26,0)-(28,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/annotations/annotations_enum.raml
