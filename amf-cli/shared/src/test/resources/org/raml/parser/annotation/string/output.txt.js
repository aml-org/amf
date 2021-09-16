ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/image/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/image/scalar_1
  Range: [(18,11)-(18,12)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be longer than 2 characters
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/foo/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/foo/scalar_1
  Range: [(19,9)-(19,12)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be shorter than 10 characters
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/tato/scalar_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/tato/scalar_1
  Range: [(20,10)-(20,15)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/string/input.raml
