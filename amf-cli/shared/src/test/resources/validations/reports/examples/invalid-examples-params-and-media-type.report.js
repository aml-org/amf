Model: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json
Profile: OAS 3.0
Conforms? true
Number of results: 6

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/array/simple/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/array/simple/examples/example/default-example
  Position: Some(LexicalInformation([(20,21)-(20,36)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/examples/example/a
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/examples/example/a
  Position: Some(LexicalInformation([(26,21)-(26,36)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be object
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/query/query-with-content/payload/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/query/query-with-content/payload/application%2Fjson/examples/example/default-example
  Position: Some(LexicalInformation([(53,23)-(53,42)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/end-points/%2Ftest%2F%7BitemId%7D/parameter/parameter/path/itemId/examples/example/a
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/end-points/%2Ftest%2F%7BitemId%7D/parameter/parameter/path/itemId/examples/example/a
  Position: Some(LexicalInformation([(73,23)-(73,25)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/end-points/%2Ftest%2F%7BitemId%7D/supportedOperation/get/expects/request%2FrequestBody/payload/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/end-points/%2Ftest%2F%7BitemId%7D/supportedOperation/get/expects/request%2FrequestBody/payload/application%2Fjson/examples/example/default-example
  Position: Some(LexicalInformation([(90,25)-(90,26)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a should be number
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/end-points/%2Ftest%2F%7BitemId%7D/supportedOperation/get/returns/resp/200/payload/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/end-points/%2Ftest%2F%7BitemId%7D/supportedOperation/get/returns/resp/200/payload/application%2Fjson/examples/example/default-example
  Position: Some(LexicalInformation([(107,27)-(107,55)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json
