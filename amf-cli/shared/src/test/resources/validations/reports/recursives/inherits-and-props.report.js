Model: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml
Profile: RAML 1.0
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: r.c should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declares/shape/A/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declares/shape/A/examples/example/invalid
  Position: Some(LexicalInformation([(18,0)-(25,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: r.c should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declares/shape/B/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declares/shape/B/examples/example/invalid
  Position: Some(LexicalInformation([(38,0)-(44,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: r.c should be string
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declares/shape/C/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declares/shape/C/examples/example/invalid
  Position: Some(LexicalInformation([(56,0)-(60,14)]))
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/inherits-and-props.raml
