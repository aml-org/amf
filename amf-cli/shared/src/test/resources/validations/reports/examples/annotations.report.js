ModelId: file://amf-cli/shared/src/test/resources/validations/annotations/annotations.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotations.raml#/web-api/customDomainProperties/b/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotations.raml#/web-api/customDomainProperties/b/scalar_1
  Range: [(10,5)-(10,7)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotations.raml
