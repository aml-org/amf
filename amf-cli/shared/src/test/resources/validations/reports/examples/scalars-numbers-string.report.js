Model: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml
Profile: RAML 1.0
Conforms? false
Number of results: 4

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidInt1
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidInt1
  Position: Some(LexicalInformation([(11,19)-(11,20)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidInt2
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidInt2
  Position: Some(LexicalInformation([(12,19)-(12,31)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidBoolean
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidBoolean
  Position: Some(LexicalInformation([(13,22)-(13,26)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidNumber
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml/declares/scalar/SomeType/examples/example/invalidNumber
  Position: Some(LexicalInformation([(14,21)-(14,24)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml
