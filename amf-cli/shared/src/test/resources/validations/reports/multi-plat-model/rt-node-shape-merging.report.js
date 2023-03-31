ModelId: file://amf-cli/shared/src/test/resources/validations/rt-node-shape-merging/api.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'a'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/rt-node-shape-merging/api.raml#/web-api/endpoint/%2Fresource/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/rt-node-shape-merging/api.raml#/web-api/endpoint/%2Fresource/supportedOperation/get/expects/request/payload/application%2Fjson/any/schema/examples/example/default-example
  Range: [(25,0)-(28,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/rt-node-shape-merging/api.raml
