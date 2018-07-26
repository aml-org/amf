Model: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json
Profile: OpenAPI
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: dateOfBirth should match format "date-time"
phoneNo should be integer

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/User/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/User/example/default-example
  Position: Some(LexicalInformation([(256,18)-(283,7)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: application.user.dateOfBirth should match format "date-time"
application.user.monthsAtCurrAdd should be integer
user.dateOfBirth should match format "date-time"
user.monthsAtCurrAdd should be integer

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/LoanApplication/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/LoanApplication/example/default-example
  Position: Some(LexicalInformation([(339,18)-(437,7)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json
