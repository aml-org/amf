Model: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: lastName should be string
name should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml#/web-api/endpoint/end-points/%2Fendpoint/supportedOperation/get/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml#/web-api/endpoint/end-points/%2Fendpoint/supportedOperation/get/expects/request/payload/application%2Fjson/shape/schema/examples/example/default-example
  Position: Some(LexicalInformation([(16,0)-(18,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/examples/declared-type-ref-add-example.raml
