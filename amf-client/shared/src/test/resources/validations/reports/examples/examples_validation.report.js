Model: file://amf-client/shared/src/test/resources/validations/examples/examples_validation.raml
Profile: RAML
Conforms? false
Number of results: 4

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":".b","schemaPath":"#/properties/b/type","params":{"type":"integer"},"message":"should be integer"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples_validation.raml#/declarations/types/A/example/default-example
  Property: 
  Position: Some(LexicalInformation([(13,0)-(16,0)]))
  Location: 

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"type","dataPath":"","schemaPath":"#/type","params":{"type":"integer"},"message":"should be integer"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples_validation.raml#/declarations/types/scalar/D/example/default-example
  Property: 
  Position: Some(LexicalInformation([(33,13)-(33,17)]))
  Location: 

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"required","dataPath":"","schemaPath":"#/required","params":{"missingProperty":"g"},"message":"should have required property 'g'"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples_validation.raml#/declarations/types/H/example/default-example
  Property: 
  Position: Some(LexicalInformation([(51,13)-(56,0)]))
  Location: 

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-client/shared/src/test/resources/validations/examples/examples_validation.raml#/declarations/types/I/type
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples_validation.raml#/declarations/types/I/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(62,12)-(67,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/examples_validation.raml
