Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: testProperty[2] should match some schema in anyOf
testProperty[2].theProperty should be equal to one of the allowed values
testProperty[3] should match some schema in anyOf
testProperty[3].mandatory should be string
testProperty[3].theProperty should be equal to one of the allowed values
testProperty[4] should match some schema in anyOf
testProperty[4].mandatory should be string
testProperty[4].theProperty should be equal to one of the allowed values
testProperty[7] should match some schema in anyOf
testProperty[7].mandatory should be string
testProperty[8] should have required property 'mandatory'
testProperty[8] should match some schema in anyOf
testProperty[8].theProperty should be equal to one of the allowed values
testProperty[9] should have required property 'mandatory'
testProperty[9] should match some schema in anyOf
testProperty[9].theProperty should be equal to one of the allowed values

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml#/declarations/types/Item/example/default-example
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml#/declarations/types/Item/example/default-example
  Position: Some(LexicalInformation([(20,0)-(38,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/union-type-not-required-property/input.raml
