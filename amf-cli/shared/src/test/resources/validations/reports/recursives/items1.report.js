ModelId: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [0].b should be string
[0].c.c should be string
[1].b should be string
[1].c.a[0].b should be string
[1].c.c should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml#/declares/array/A/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml#/declares/array/A/examples/example/invalid
  Range: [(18,0)-(26,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be string
c.a[0].b should be string
c.c should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml#/declares/shape/B/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml#/declares/shape/B/examples/example/invalid
  Range: [(39,0)-(44,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a[0].b should be string
a[1].b should be string
a[1].c.c should be string
c should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml#/declares/shape/C/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml#/declares/shape/C/examples/example/invalid
  Range: [(57,0)-(63,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/items1.raml
