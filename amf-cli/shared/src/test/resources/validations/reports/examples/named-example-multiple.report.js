Model: file://amf-cli/shared/src/test/resources/validations/examples/named-example-multiple/api.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'age'
should have required property 'name'

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/named-example-multiple/api.raml#/declares/Person/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/named-example-multiple/api.raml#/declares/Person/examples/example/default-example
  Position: Some(LexicalInformation([(2,0)-(7,9)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/named-example-multiple/example.raml
