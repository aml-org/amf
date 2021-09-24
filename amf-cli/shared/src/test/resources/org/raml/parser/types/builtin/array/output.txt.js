ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 5

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: employees[0].age should be >= 0
employees[1].email should match pattern "^.+@.+\..+$"
employees[2].name should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/shape/Office/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/shape/Office/examples/example/default-example
  Range: [(19,12)-(42,13)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have fewer than 2 items
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-min
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-min
  Range: [(49,15)-(49,21)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 3 items
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-max
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-max
  Range: [(50,15)-(50,39)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-type
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-type
  Range: [(51,16)-(51,20)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-type2
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declares/array/Colors/examples/example/bad-type2
  Range: [(53,0)-(54,19)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml
