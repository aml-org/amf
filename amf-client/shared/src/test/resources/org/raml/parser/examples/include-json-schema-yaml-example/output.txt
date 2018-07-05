Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml#/declarations/types/User_validation
  Message: Object at / must be valid
Scalar at //age must have data type http://www.w3.org/2001/XMLSchema#integer

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml#/declarations/types/User/example/default-example
  Property: http://a.ml/vocabularies/data#age
  Position: Some(LexicalInformation([(7,0)-(9,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema-yaml-example/input.raml
