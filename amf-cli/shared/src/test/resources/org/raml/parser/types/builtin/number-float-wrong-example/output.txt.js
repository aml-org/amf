Model: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be multiple of 3
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml/declares/scalar/MyCustomType/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml/declares/scalar/MyCustomType/examples/example/default-example
  Position: Some(LexicalInformation([(10,15)-(10,20)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be >= 2.5
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml/declares/scalar/OtherCustomType/examples/example/bad1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml/declares/scalar/OtherCustomType/examples/example/bad1
  Position: Some(LexicalInformation([(18,14)-(18,17)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 5.3
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml/declares/scalar/OtherCustomType/examples/example/bad2
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml/declares/scalar/OtherCustomType/examples/example/bad2
  Position: Some(LexicalInformation([(19,14)-(19,17)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/number-float-wrong-example/input.raml
