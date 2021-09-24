ModelId: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid.raml
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#closed-shape
  Message: Property 'application/json' not supported in a RAML 1.0 response node
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid.raml#/web-api/endpoint/%2Ftest%2F%7Bid%7D/supportedOperation/patch/returns/resp/body
  Property: 
  Range: [(13,12)-(14,26)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid.raml
