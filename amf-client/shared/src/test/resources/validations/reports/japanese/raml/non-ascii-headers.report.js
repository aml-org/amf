Model: file://amf-client/shared/src/test/resources/validations/japanese/raml/non-ascii-headers.raml
Profile: RAML 1.0
Conforms? true
Number of results: 1

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#mandatory-header-name-pattern
  Message: Header name must comply RFC-7230
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/japanese/raml/non-ascii-headers.raml#/web-api/end-points/%2Ffoo-resources/post/request/parameter/header/%E3%83%AD%E3%83%BC%E3%83%B3%E7%94%B3%E3%81%97%E8%BE%BC%E3%81%BF
  Property: 
  Position: Some(LexicalInformation([(8,6)-(9,29)]))
  Location: file://amf-client/shared/src/test/resources/validations/japanese/raml/non-ascii-headers.raml
