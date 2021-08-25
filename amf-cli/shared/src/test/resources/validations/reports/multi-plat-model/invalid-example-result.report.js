Model: file://amf-cli/shared/src/test/resources/validations/raml/nested-json-schema-external-fragment/api.raml
Profile: RAML 0.8
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: A should be integer
should have required property 'B'

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/raml/nested-json-schema-external-fragment/api.raml#/web-api/endpoint/%2FsomeEndpoint/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/raml/nested-json-schema-external-fragment/api.raml#/web-api/endpoint/%2FsomeEndpoint/supportedOperation/get/returns/resp/200/payload/application%2Fjson/shape/application%2Fjson/examples/example/default-example
  Position: Some(LexicalInformation([(13,24)-(15,25)]))
  Location: file://amf-cli/shared/src/test/resources/validations/raml/nested-json-schema-external-fragment/api.raml
