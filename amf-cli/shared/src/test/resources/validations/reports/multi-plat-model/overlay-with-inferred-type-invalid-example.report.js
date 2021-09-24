ModelId: file://amf-cli/shared/src/test/resources/validations/overlays/overlay-with-inferred-type-invalid-example/overlay.raml#/references/0
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: id should NOT be longer than 3 characters
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/overlays/overlay-with-inferred-type-invalid-example/overlay.raml#/references/0/declares/shape/TestEntity/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/overlays/overlay-with-inferred-type-invalid-example/overlay.raml#/references/0/declares/shape/TestEntity/example/default-example
  Range: [(8,13)-(10,14)]
  Location: file://amf-cli/shared/src/test/resources/validations/overlays/overlay-with-inferred-type-invalid-example/overlay.raml
