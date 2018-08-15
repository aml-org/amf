Model: file://amf-client/shared/src/test/resources/validations/examples/min-max-properties-example.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should NOT have less than 2 properties
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/min-max-properties-example.raml#/declarations/types/InvalidMax/example/badMin
  Property: 
  Position: Some(LexicalInformation([(15,0)-(16,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/min-max-properties-example.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should NOT have more than 2 properties
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/min-max-properties-example.raml#/declarations/types/InvalidMax/example/badMax
  Property: 
  Position: Some(LexicalInformation([(17,0)-(20,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/min-max-properties-example.raml
