Model: file://amf-client/shared/src/test/resources/validations/examples/double-example-inttype.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message:  should be integer
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/double-example-inttype.raml#/declarations/types/orders/property/Priority/scalar/Priority/example/default-example
  Property: 
  Position: Some(LexicalInformation([(9,17)-(9,20)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/double-example-inttype.raml
