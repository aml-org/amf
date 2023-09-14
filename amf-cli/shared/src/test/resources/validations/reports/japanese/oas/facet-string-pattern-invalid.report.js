ModelId: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml
Profile: OAS 3.0
Conforms: true
Number of results: 1

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match pattern "^([\u30a0-\u30ff]+)"
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml#/web-api/endpoint/%2Fping/supportedOperation/post/pingOp/expects/request%2FrequestBody/payload/application%2Fjson/scalar/SomeObj/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml#/web-api/endpoint/%2Fping/supportedOperation/post/pingOp/expects/request%2FrequestBody/payload/application%2Fjson/scalar/SomeObj/examples/example/default-example
  Range: [(28,15)-(28,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml
