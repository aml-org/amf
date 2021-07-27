Model: file://amf-cli/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
should be string
should match some schema in anyOf

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml/union/type/examples/example/ex3
  Property: file://amf-cli/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml/union/type/examples/example/ex3
  Position: Some(LexicalInformation([(8,0)-(9,22)]))
  Location: file://amf-cli/shared/src/test/resources/validations/shapes/invalid-example-in-unions.raml
