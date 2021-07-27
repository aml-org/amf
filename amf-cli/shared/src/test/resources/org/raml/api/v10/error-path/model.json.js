Model: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 6

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [0][1] should be number
[1][1] should be number

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/array/Matrix/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/array/Matrix/examples/example/default-example
  Position: Some(LexicalInformation([(6,13)-(6,39)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: family[1] should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/User/examples/example/badExample
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/User/examples/example/badExample
  Position: Some(LexicalInformation([(26,0)-(31,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'age'
should have required property 'family'

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/User/examples/example/anotherBadExample
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/User/examples/example/anotherBadExample
  Position: Some(LexicalInformation([(32,0)-(33,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/User/examples/example/anotherOne
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/User/examples/example/anotherOne
  Position: Some(LexicalInformation([(34,0)-(39,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: users[1].family[1] should be string
users[1].family[2] should be string
users[2].age should be integer

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/Book/examples/example/one
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/Book/examples/example/one
  Position: Some(LexicalInformation([(51,0)-(72,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: authors[1] should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/Book/examples/example/two
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml/declares/Book/examples/example/two
  Position: Some(LexicalInformation([(73,0)-(84,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml
