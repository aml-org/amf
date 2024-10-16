ModelId: file://amf-cli/shared/src/test/resources/validations/japanese/oas/non-ascii-headers.json
Profile: OAS 2.0
Conforms: true
Number of results: 1

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/parser#mandatory-header-name-pattern
  Message: Header name must comply RFC-7230
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/japanese/oas/non-ascii-headers.json#/web-api/endpoint/%2F%E3%83%AD%E3%83%BC%E3%83%B3%E7%94%B3%E3%81%97%E8%BE%BC%E3%81%BF/supportedOperation/post/expects/request/header/parameter/header/%E3%83%AD%E3%83%BC%E3%83%B3%E7%94%B3%E3%81%97%E8%BE%BC%E3%81%BF
  Property: 
  Range: [(11,10)-(17,11)]
  Location: file://amf-cli/shared/src/test/resources/validations/japanese/oas/non-ascii-headers.json
