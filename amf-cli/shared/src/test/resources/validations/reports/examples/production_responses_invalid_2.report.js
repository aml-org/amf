ModelId: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid-2.raml
Profile: RAML 0.8
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#Response-statusCode-pattern
  Message: Status code for a Response must be a value between 100 and 599
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid-2.raml#/web-api/endpoint/%2Fsystem%2Ftoken/supportedOperation/post/returns/resp/404.14
  Property: http://a.ml/vocabularies/apiContract#statusCode
  Range: [(9,6)-(9,12)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid-2.raml
