Model: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: startDate should match format "date"
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml#/web-api/endpoint/end-points/%2Fsubscription/supportedOperation/post/expects/request/payload/application%2Fjson/shape/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml#/web-api/endpoint/end-points/%2Fsubscription/supportedOperation/post/expects/request/payload/application%2Fjson/shape/application%2Fjson/examples/example/default-example
  Position: Some(LexicalInformation([(1,0)-(4,1)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/example.json

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(9,8)-(9,14)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml
