ModelId: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json
Profile: OAS 3.0
Conforms: true
Number of results: 1

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT be valid
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json#/web-api/endpoint/%2Ftest/supportedOperation/get/returns/resp/200/payload/application%2Fjson/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json#/web-api/endpoint/%2Ftest/supportedOperation/get/returns/resp/200/payload/application%2Fjson/examples/example/invalid
  Range: [(25,29)-(25,42)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/not-constraint.json
