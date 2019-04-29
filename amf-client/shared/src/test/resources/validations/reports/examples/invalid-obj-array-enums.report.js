Model: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml#/declarations/types/array/A/examples/example/invalid1
  Property: 
  Position: Some(LexicalInformation([(22,0)-(24,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml#/declarations/types/array/A/examples/example/invalid2
  Property: 
  Position: Some(LexicalInformation([(25,0)-(29,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/enums/invalid-obj-array-enums.raml
