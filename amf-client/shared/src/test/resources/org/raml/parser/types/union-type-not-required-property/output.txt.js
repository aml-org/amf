Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml#/declarations/types/Item_validation
  Message: Object at / must be valid
Array items at //testProperty must be valid
Data at //testProperty/items must be one of the valid union types: FirstOption, SecondOption

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml#/declarations/types/Item/example/default-example
  Property: http://a.ml/vocabularies/data#testProperty
  Position: Some(LexicalInformation([(20,0)-(38,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml
