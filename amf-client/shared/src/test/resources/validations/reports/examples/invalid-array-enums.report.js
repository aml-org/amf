Model: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid1
  Property: 
  Position: Some(LexicalInformation([(10,16)-(10,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml#/declarations/types/array/A/example/invalid2
  Property: 
  Position: Some(LexicalInformation([(11,16)-(11,20)]))
  Location: file://amf-client/shared/src/test/resources/validations/enums/invalid-array-enums.raml
