ModelId: file://amf-cli/shared/src/test/resources/validations/raml/discriminator-value-conforms-against-discriminator-property/api.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/discriminator-value-conforms-against-discriminator-property/api.raml#/declares/shape/Pet/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/raml/discriminator-value-conforms-against-discriminator-property/api.raml#/declares/shape/Pet/scalar_1
  Range: [(8,24)-(8,28)]
  Location: file://amf-cli/shared/src/test/resources/validations/raml/discriminator-value-conforms-against-discriminator-property/api.raml
