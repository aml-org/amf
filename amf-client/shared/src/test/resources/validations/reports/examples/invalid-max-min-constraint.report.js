Model: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml
Profile: RAML
Conforms? false
Number of results: 6

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"minimum","dataPath":"","schemaPath":"#/minimum","params":{"comparison":">=","limit":2.4,"exclusive":false},"message":"should be >= 2.4"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml#/declarations/types/scalar/OtherCustomType/example/bad1
  Property: 
  Position: Some(LexicalInformation([(10,12)-(10,13)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"minimum","dataPath":"","schemaPath":"#/minimum","params":{"comparison":">=","limit":2.4,"exclusive":false},"message":"should be >= 2.4"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml#/declarations/types/scalar/OtherCustomType/example/bad2
  Property: 
  Position: Some(LexicalInformation([(11,12)-(11,15)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"minimum","dataPath":"","schemaPath":"#/minimum","params":{"comparison":">=","limit":2.4,"exclusive":false},"message":"should be >= 2.4"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml#/declarations/types/scalar/OtherCustomType/example/bad3
  Property: 
  Position: Some(LexicalInformation([(12,12)-(12,15)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"maximum","dataPath":"","schemaPath":"#/maximum","params":{"comparison":"<=","limit":5.3,"exclusive":false},"message":"should be <= 5.3"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml#/declarations/types/scalar/OtherCustomType/example/bad4
  Property: 
  Position: Some(LexicalInformation([(13,12)-(13,15)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"maximum","dataPath":"","schemaPath":"#/maximum","params":{"comparison":"<=","limit":5.3,"exclusive":false},"message":"should be <= 5.3"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml#/declarations/types/scalar/OtherCustomType/example/bad5
  Property: 
  Position: Some(LexicalInformation([(14,12)-(14,15)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"maximum","dataPath":"","schemaPath":"#/maximum","params":{"comparison":"<=","limit":5.3,"exclusive":false},"message":"should be <= 5.3"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml#/declarations/types/scalar/OtherCustomType/example/bad6
  Property: 
  Position: Some(LexicalInformation([(15,12)-(15,13)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/invalid-max-min-constraint.raml
