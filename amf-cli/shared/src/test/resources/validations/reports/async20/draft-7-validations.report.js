Model: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml
Profile: ASYNC 2.0
Conforms? false
Number of results: 9

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/scalar/const-keyword/examples/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/scalar/const-keyword/examples/example/default-example_1
  Position: Some(LexicalInformation([(12,8)-(12,17)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: postal_code should match pattern "[0-9]{5}(-[0-9]{4})?"
should match "then" schema

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/conditional-subschemas/examples/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/conditional-subschemas/examples/example/default-example_1
  Position: Some(LexicalInformation([(32,6)-(35,7)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: postal_code should match pattern "[A-Z][0-9][A-Z] [0-9][A-Z][0-9]"
should match "else" schema

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/conditional-subschemas/examples/example/default-example_4
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/conditional-subschemas/examples/example/default-example_4
  Position: Some(LexicalInformation([(44,6)-(47,7)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: property name 'invalid5' is invalid
should match pattern "^[A-Za-z]*$"

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/property-names/examples/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/property-names/examples/example/default-example_1
  Position: Some(LexicalInformation([(55,7)-(55,30)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [0] should be number
should contain a valid item

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/array/contains-keyword/examples/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/array/contains-keyword/examples/example/default-example_1
  Position: Some(LexicalInformation([(63,8)-(63,19)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [2] should be number
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/array/additional-items/examples/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/array/additional-items/examples/example/default-example_1
  Position: Some(LexicalInformation([(80,8)-(80,32)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be > 0
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/exclusive-with-values/examples/example/default-example_1
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/exclusive-with-values/examples/example/default-example_1
  Position: Some(LexicalInformation([(96,8)-(99,9)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a should be < 100
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/exclusive-with-values/examples/example/default-example_2
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/shape/exclusive-with-values/examples/example/default-example_2
  Position: Some(LexicalInformation([(100,8)-(103,9)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be valid
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/any/notKeyword/examples/example/default-example_2
  Property: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml#/declares/any/notKeyword/examples/example/default-example_2
  Position: Some(LexicalInformation([(122,8)-(125,9)]))
  Location: file://amf-cli/shared/src/test/resources/validations/async20/validations/draft-7-validations.yaml
