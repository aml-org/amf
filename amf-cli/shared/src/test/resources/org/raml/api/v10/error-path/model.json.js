ModelId: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 6

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [0][1] should be number
[1][1] should be number

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/array/Matrix/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/array/Matrix/examples/example/default-example
  Range: [(6,13)-(6,39)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: family[1] should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/User/examples/example/badExample
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/User/examples/example/badExample
  Range: [(26,0)-(31,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'age'
should have required property 'family'

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/User/examples/example/anotherBadExample
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/User/examples/example/anotherBadExample
  Range: [(32,0)-(33,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/User/examples/example/anotherOne
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/User/examples/example/anotherOne
  Range: [(34,0)-(39,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: users[1].family[1] should be string
users[1].family[2] should be string
users[2].age should be integer

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/Book/examples/example/one
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/Book/examples/example/one
  Range: [(51,0)-(72,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: authors[1] should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/Book/examples/example/two
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml#/declares/shape/Book/examples/example/two
  Range: [(73,0)-(84,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/error-path/input.raml
