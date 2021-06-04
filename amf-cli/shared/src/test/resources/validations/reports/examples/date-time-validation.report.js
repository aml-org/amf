Model: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml
Profile: RAML 1.0
Conforms? false
Number of results: 6

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date-time-only"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/fireworks/example/bad
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/fireworks/example/bad
  Position: Some(LexicalInformation([(8,11)-(8,30)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/birthday/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/birthday/example/default-example
  Position: Some(LexicalInformation([(11,13)-(11,23)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "time"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/lunchtime/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/lunchtime/example/default-example
  Position: Some(LexicalInformation([(14,13)-(14,21)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date-time"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/created/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/created/example/default-example
  Position: Some(LexicalInformation([(17,13)-(17,37)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "rfc2616"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since/example/default-example
  Position: Some(LexicalInformation([(21,13)-(21,42)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match format "date-time"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since2/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml#/declarations/types/scalar/If-Modified-Since2/example/default-example
  Position: Some(LexicalInformation([(25,13)-(25,38)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/date_time_validations2.raml
