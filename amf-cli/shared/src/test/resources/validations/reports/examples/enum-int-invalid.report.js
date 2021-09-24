ModelId: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml#/declares/scalar/A/in/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml#/declares/scalar/A/in/scalar_1
  Range: [(6,13)-(6,14)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml#/declares/scalar/A/in/scalar_2
  Property: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml#/declares/scalar/A/in/scalar_2
  Range: [(6,15)-(6,16)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml#/declares/scalar/A/in/scalar_3
  Property: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml#/declares/scalar/A/in/scalar_3
  Range: [(6,17)-(6,18)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/enum-int-invalid.raml
