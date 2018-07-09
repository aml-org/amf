Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declarations/types/User_validation
  Message: Object at / must be valid
Property at //middlename must be null
Scalar at //lastname must have data type http://www.w3.org/2001/XMLSchema#string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declarations/types/User/example/wrong-type
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declarations/types/User/example/wrong-type
  Position: Some(LexicalInformation([(15,0)-(18,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declarations/types/User_validation_middlename_validation_minCount/prop
  Message: Data at //middlename must have min. cardinality 1
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml#/declarations/types/User/example/missing-field
  Property: http://a.ml/vocabularies/data#middlename
  Position: Some(LexicalInformation([(19,0)-(21,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/null/input.raml
