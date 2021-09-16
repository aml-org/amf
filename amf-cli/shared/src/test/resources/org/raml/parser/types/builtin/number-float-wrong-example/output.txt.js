ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be multiple of 3
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml#/declarations/types/scalar/MyCustomType/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml#/declarations/types/scalar/MyCustomType/example/default-example
  Range: [(10,15)-(10,20)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be >= 2.5
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml#/declarations/types/scalar/OtherCustomType/example/bad1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml#/declarations/types/scalar/OtherCustomType/example/bad1
  Range: [(18,14)-(18,17)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 5.3
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml#/declarations/types/scalar/OtherCustomType/example/bad2
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml#/declarations/types/scalar/OtherCustomType/example/bad2
  Range: [(19,14)-(19,17)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml
