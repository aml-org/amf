Model: file://amf-client/shared/src/test/resources/validations/recursives/inherits-and-props.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: r.c should be string
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declarations/types/A/example/invalid
  Property: 
  Position: Some(LexicalInformation([(18,0)-(25,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/recursives/inherits-and-props.raml

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: r.c should be string
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/recursives/inherits-and-props.raml#/declarations/types/C/example/invalid
  Property: 
  Position: Some(LexicalInformation([(56,0)-(60,14)]))
  Location: file://amf-client/shared/src/test/resources/validations/recursives/inherits-and-props.raml
