ModelId: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml
Profile: RAML 1.0
Conforms: false
Number of results: 4

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidInt1
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidInt1
  Range: [(11,19)-(11,20)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidInt2
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidInt2
  Range: [(12,19)-(12,31)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidBoolean
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidBoolean
  Range: [(13,22)-(13,26)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidNumber
  Property: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declares/scalar/SomeType/examples/example/invalidNumber
  Range: [(14,21)-(14,24)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/string-hierarchy.raml
