Model: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/declares/User/examples/example/bad
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/declares/User/examples/example/bad
  Position: Some(LexicalInformation([(13,10)-(16,11)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/web-api/endpoint/end-points/%2Fsend/supportedOperation/post/expects/request/payload/application%2Fjson/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/web-api/endpoint/end-points/%2Fsend/supportedOperation/post/expects/request/payload/application%2Fjson/schema/examples/example/default-example
  Position: Some(LexicalInformation([(23,16)-(26,17)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml
