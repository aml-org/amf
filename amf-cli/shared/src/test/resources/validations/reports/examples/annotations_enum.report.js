ModelId: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: items should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/test/object_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/test/object_1
  Range: [(23,0)-(25,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: items should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/testInt/object_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/testInt/object_1
  Range: [(26,0)-(28,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml
