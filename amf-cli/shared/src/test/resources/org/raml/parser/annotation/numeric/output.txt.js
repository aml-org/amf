ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 4

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be >= 5
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2FpersonNotOk1/age/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2FpersonNotOk1/age/scalar_1
  Range: [(23,9)-(23,10)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2FpersonNotOk2/fingers/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2FpersonNotOk2/fingers/scalar_1
  Range: [(27,13)-(27,15)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 10
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2Frange/range/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2Frange/range/scalar_1
  Range: [(29,11)-(29,13)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be multiple of 4
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2FyearsBad/leapYear/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml#/web-api/end-points/%2FyearsBad/leapYear/scalar_1
  Range: [(33,14)-(33,18)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/numeric/input.raml
