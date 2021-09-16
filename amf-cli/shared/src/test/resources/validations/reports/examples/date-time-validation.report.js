ModelId: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml
Profile: RAML 1.0
Conforms: false
Number of results: 6

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date-time-only"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/fireworks/example/bad
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/fireworks/example/bad
  Range: [(8,11)-(8,30)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/birthday/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/birthday/example/default-example
  Range: [(11,13)-(11,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "time"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/lunchtime/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/lunchtime/example/default-example
  Range: [(14,13)-(14,21)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date-time"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/created/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/created/example/default-example
  Range: [(17,13)-(17,37)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "rfc2616"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since/example/default-example
  Range: [(21,13)-(21,42)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date-time"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since2/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since2/example/default-example
  Range: [(25,13)-(25,38)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml
