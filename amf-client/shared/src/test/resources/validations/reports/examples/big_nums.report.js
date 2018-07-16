Model: file://amf-client/shared/src/test/resources/validations/types/big_nums.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"maximum","dataPath":"","schemaPath":"#/maximum","params":{"comparison":"<=","limit":999999999999,"exclusive":false},"message":"should be <= 999999999999"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/types/big_nums.raml#/declarations/types/scalar/LongNumber/example/three
  Property: 
  Position: Some(LexicalInformation([(11,13)-(11,26)]))
  Location: file://amf-client/shared/src/test/resources/validations/types/big_nums.raml
