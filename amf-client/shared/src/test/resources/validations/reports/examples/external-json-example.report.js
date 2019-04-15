Model: file://amf-client/shared/src/test/resources/validations/examples/external-json/api.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should have required property 'lastName'
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/external-json/api.raml#/declarations/types/a/examples/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(4,1)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/external-json/example.json
