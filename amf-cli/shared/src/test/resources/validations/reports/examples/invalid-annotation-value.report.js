ModelId: file://amf-cli/shared/src/test/resources/validations/examples/invalid-annotation-value.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be number
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/invalid-annotation-value.raml#/web-api/end-points/%2Fusers/intAnnotation/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/examples/invalid-annotation-value.raml#/web-api/end-points/%2Fusers/intAnnotation/scalar_1
  Range: [(26,19)-(26,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/invalid-annotation-value.raml
