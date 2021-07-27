Model: file://amf-cli/shared/src/test/resources/validations/custom-js-validations/mutiple-of.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#ScalarShape-multipleOf-minExclusive
  Message: multipleOf facet for a RAML scalar type must be greater than 0
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/custom-js-validations/mutiple-of.raml/declares/scalar/error
  Property: http://a.ml/vocabularies/shapes#multipleOf
  Position: Some(LexicalInformation([(7,16)-(7,17)]))
  Location: file://amf-cli/shared/src/test/resources/validations/custom-js-validations/mutiple-of.raml
