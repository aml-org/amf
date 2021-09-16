ModelId: file://amf-cli/shared/src/test/resources/validations/annotation-types-invalid-example.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotation-types-invalid-example.raml#/web-api/apigateway-integration/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotation-types-invalid-example.raml#/web-api/apigateway-integration/scalar_1
  Range: [(9,26)-(9,28)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotation-types-invalid-example.raml
