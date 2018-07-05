Model: file://amf-client/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml#/declarations/types/scalar/DefaultMaxLength_validation_validation_minLength/prop
  Message: Data at / must have length greater than 10
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml#/declarations/types/scalar/DefaultMaxLength/example/two
  Property: http://a.ml/vocabularies/data#value
  Position: Some(LexicalInformation([(14,13)-(14,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/types/facet-validation/string/input.raml
