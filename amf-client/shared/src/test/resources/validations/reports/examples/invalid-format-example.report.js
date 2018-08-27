Model: file://amf-client/shared/src/test/resources/validations/examples/invalid-format-example.raml
Profile: RAML 1.0
Conforms? true
Number of results: 1

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#parsing-warning
  Message: Format int64 is not valid for type http://www.w3.org/2001/XMLSchema#integer
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-format-example.raml#/declarations/types/scalar/createdTimestamp
  Property: 
  Position: Some(LexicalInformation([(6,6)-(7,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-format-example.raml
