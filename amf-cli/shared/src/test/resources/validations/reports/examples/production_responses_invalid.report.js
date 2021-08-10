Model: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid.raml
Profile: 
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#closed-shape
  Message: Property 'application/json' not supported in a RAML 1.0 response node
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid.raml/web-api/end-points/%2Ftest%2F%7Bid%7D/patch/resp/body
  Property: 
  Position: Some(LexicalInformation([(13,12)-(14,26)]))
  Location: file://amf-cli/shared/src/test/resources/validations/production/responses-invalid.raml
