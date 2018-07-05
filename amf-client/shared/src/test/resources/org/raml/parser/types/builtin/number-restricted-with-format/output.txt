Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml#/declarations/types/orders_validation
  Message: Object at / must be valid
Scalar at //Priority must have data type http://www.w3.org/2001/XMLSchema#integer

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml#/web-api/end-points/%2Fresource/post/request/application%2Fjson/schema/example/default-example
  Property: http://a.ml/vocabularies/data#Priority
  Position: Some(LexicalInformation([(15,0)-(16,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/builtin/number-restricted-with-format/input.raml
