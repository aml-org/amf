Model: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json
Profile: OpenAPI
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"format","dataPath":".dateOfBirth","schemaPath":"#/properties/dateOfBirth/format","params":{"format":"date-time"},"message":"should match format \"date-time\""}
{"keyword":"type","dataPath":".phoneNo","schemaPath":"#/properties/phoneNo/type","params":{"type":"integer"},"message":"should be integer"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/User/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/User/example/default-example
  Position: Some(LexicalInformation([(256,18)-(283,7)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"format","dataPath":".application.user.dateOfBirth","schemaPath":"#/properties/application/properties/user/properties/dateOfBirth/format","params":{"format":"date-time"},"message":"should match format \"date-time\""}
{"keyword":"format","dataPath":".user.dateOfBirth","schemaPath":"#/properties/user/properties/dateOfBirth/format","params":{"format":"date-time"},"message":"should match format \"date-time\""}
{"keyword":"type","dataPath":".application.user.monthsAtCurrAdd","schemaPath":"#/properties/application/properties/user/properties/monthsAtCurrAdd/type","params":{"type":"integer"},"message":"should be integer"}
{"keyword":"type","dataPath":".user.monthsAtCurrAdd","schemaPath":"#/properties/user/properties/monthsAtCurrAdd/type","params":{"type":"integer"},"message":"should be integer"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/LoanApplication/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json#/declarations/types/LoanApplication/example/default-example
  Position: Some(LexicalInformation([(339,18)-(437,7)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/examples-in-oas.json
