Model: file://amf-client/shared/src/test/resources/validations/examples/invalid-nested-named-example-3/api.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#named-example-used-inlined-example
  Message: Named example fragments must be included in 'examples' facet
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-nested-named-example-3/person-example.raml
  Property: 
  Position: Some(LexicalInformation([(4,10)-(4,38)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-nested-named-example-3/employee-example.raml
