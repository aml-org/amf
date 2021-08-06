Model: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml
Profile: RAML 1.0
Conforms? false
Number of results: 4

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10000000000000000
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigNumberMax/examples/example/wrong2
  Property: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigNumberMax/examples/example/wrong2
  Position: Some(LexicalInformation([(11,14)-(11,31)]))
  Location: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10000000000000000
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigNumberMax/examples/example/wrong3
  Property: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigNumberMax/examples/example/wrong3
  Position: Some(LexicalInformation([(12,14)-(12,31)]))
  Location: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10000000000000000
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigIntMax/examples/example/wrong2
  Property: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigIntMax/examples/example/wrong2
  Position: Some(LexicalInformation([(28,14)-(28,31)]))
  Location: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10000000000000000
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigIntMax/examples/example/wrong3
  Property: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml#/declares/scalar/BigIntMax/examples/example/wrong3
  Position: Some(LexicalInformation([(29,14)-(29,31)]))
  Location: file://amf-cli/shared/src/test/resources/validations/raml/big-number-examples.raml
