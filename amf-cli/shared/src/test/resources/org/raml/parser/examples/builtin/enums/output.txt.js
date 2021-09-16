ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/enums/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/enums/input.raml#/declarations/types/scalar/countryBad/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/enums/input.raml#/declarations/types/scalar/countryBad/example/default-example
  Range: [(11,13)-(11,16)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/enums/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/enums/input.raml#/declarations/types/scalar/sizesBad/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/enums/input.raml#/declarations/types/scalar/sizesBad/example/default-example
  Range: [(19,13)-(19,14)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/builtin/enums/input.raml
