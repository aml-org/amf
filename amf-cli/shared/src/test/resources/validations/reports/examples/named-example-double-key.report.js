Model: file://amf-cli/shared/src/test/resources/validations/examples/named-example-double-key/api.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'age'
should have required property 'name'

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/named-example-double-key/api.raml#/declares/Person/examples/example/a
  Property: file://amf-cli/shared/src/test/resources/validations/examples/named-example-double-key/api.raml#/declares/Person/examples/example/a
  Position: Some(LexicalInformation([(2,0)-(4,9)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/named-example-double-key/example.raml
