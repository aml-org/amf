Model: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have fewer than 2 items
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declares/array/Colors/examples/example/bad-min
  Property: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declares/array/Colors/examples/example/bad-min
  Position: Some(LexicalInformation([(10,15)-(10,21)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 3 items
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declares/array/Colors/examples/example/bad-max
  Property: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml#/declares/array/Colors/examples/example/bad-max
  Position: Some(LexicalInformation([(11,15)-(11,39)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/min-max-items.raml
