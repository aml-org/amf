Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml
Profile: RAML
Conforms? false
Number of results: 5

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/Office_validation
  Message: Object at / must be valid
Array items at //employees must be valid
Data at //employees/items/age must be greater than or equal to 0
Data at //employees/items/email must match pattern ^.+@.+\..+$

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/Office/example/default-example
  Property: http://a.ml/vocabularies/data#employees
  Position: Some(LexicalInformation([(18,13)-(43,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors_validation_validation_minItems/prop
  Message: Number of items at / must be greater than 2
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-min
  Property: http://www.w3.org/1999/02/22-rdf-syntax-ns#member
  Position: Some(LexicalInformation([(49,15)-(49,21)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors_validation_validation_maxItems/prop
  Message: Number of items at / must be smaller than 3
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-max
  Property: http://www.w3.org/1999/02/22-rdf-syntax-ns#member
  Position: Some(LexicalInformation([(50,15)-(50,39)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors_validation_validation_minItems/prop
  Message: Number of items at / must be greater than 2
Data at / must be an array

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type
  Position: Some(LexicalInformation([(51,16)-(51,20)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors_validation_validation_minItems/prop
  Message: Number of items at / must be greater than 2
Data at / must be an array

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type2
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml#/declarations/types/array/Colors/example/bad-type2
  Position: Some(LexicalInformation([(53,0)-(54,19)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/array/input.raml
