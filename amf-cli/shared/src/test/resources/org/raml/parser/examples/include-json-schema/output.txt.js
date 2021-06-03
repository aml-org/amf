Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/declarations/types/User/example/bad
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/declarations/types/User/example/bad
  Position: Some(LexicalInformation([(13,10)-(16,11)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: age should be integer
name should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/web-api/end-points/%2Fsend/post/request/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml#/web-api/end-points/%2Fsend/post/request/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(23,16)-(26,17)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/include-json-schema/input.raml
