ModelId: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml
Profile: RAML 1.0
Conforms: false
Number of results: 4

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/A/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/A/examples/example/default-example
  Range: [(13,0)-(16,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/scalar/D/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/scalar/D/examples/example/default-example
  Range: [(33,13)-(33,17)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'g'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/H/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/H/examples/example/default-example
  Range: [(52,12)-(55,13)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/I
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml#/declares/shape/I/examples/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Range: [(62,12)-(67,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples_validation.raml
