Model: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 5

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: employees[0].age should be >= 0
employees[1].email should match pattern "^.+@.+\..+$"
employees[2].name should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/Office/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/Office/example/default-example
  Position: Some(LexicalInformation([(19,12)-(42,13)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have less than 2 items
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-min
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-min
  Position: Some(LexicalInformation([(49,15)-(49,21)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 3 items
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-max
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-max
  Position: Some(LexicalInformation([(50,15)-(50,39)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type
  Position: Some(LexicalInformation([(51,16)-(51,20)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type2
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type2
  Position: Some(LexicalInformation([(53,0)-(54,19)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml
