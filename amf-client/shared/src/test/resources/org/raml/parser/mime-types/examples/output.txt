Model: file://amf-client/shared/src/test/resources/org/raml/parser/mime-types/examples/input.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/mime-types/examples/input.raml#/declarations/types/User_validation
  Message: Object at / must be valid
Scalar at //age must have data type http://a.ml/vocabularies/shapes#number

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/mime-types/examples/input.raml#/web-api/end-points/%2Ftop/post/request/application%2Fjson/schema/example/errors
  Property: http://a.ml/vocabularies/data#age
  Position: Some(LexicalInformation([(25,28)-(31,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/mime-types/examples/input.raml
