ModelId: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a should be string
b.a.a should be string
b.b should be string
c.b.a.a should be string
c.b.b should be string
c.c should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml#/declarations/types/A/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml#/declarations/types/A/example/invalid
  Range: [(23,0)-(34,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a.a should be string
a.c.b.a.a should be string
a.c.b.b should be string
a.c.c should be string
b should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml#/declarations/types/B/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml#/declarations/types/B/example/invalid
  Range: [(46,0)-(55,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b.b should be string
c should be string

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml#/declarations/types/C/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml#/declarations/types/C/example/invalid
  Range: [(67,0)-(71,17)]
  Location: file://amf-cli/shared/src/test/resources/validations/recursives/props3.raml
