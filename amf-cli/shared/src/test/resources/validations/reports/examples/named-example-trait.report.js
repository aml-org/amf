Model: file://amf-cli/shared/src/test/resources/validations/examples/named-example-trait/api.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#named-example-used-inlined-example
  Message: Named example fragments must be included in 'examples' facet
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/named-example-trait/example.raml
  Property: 
  Position: Some(LexicalInformation([(21,10)-(21,35)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/named-example-trait/api.raml
