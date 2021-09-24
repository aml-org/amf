ModelId: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json
Profile: OAS 3.0
Conforms: true
Number of results: 6

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/array/simple/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/array/simple/examples/example/default-example
  Range: [(20,21)-(20,36)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be array
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/examples/example/a
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/header/simple/examples/example/a
  Range: [(26,21)-(26,36)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be object
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/query/query-with-content/payload/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/declares/parameter/query/query-with-content/payload/application%2Fjson/examples/example/default-example
  Range: [(53,23)-(53,42)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/%2Ftest%2F%7BitemId%7D/parameter/parameter/path/itemId/examples/example/a
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/%2Ftest%2F%7BitemId%7D/parameter/parameter/path/itemId/examples/example/a
  Range: [(73,23)-(73,25)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/%2Ftest%2F%7BitemId%7D/supportedOperation/get/expects/request%2FrequestBody/payload/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/%2Ftest%2F%7BitemId%7D/supportedOperation/get/expects/request%2FrequestBody/payload/application%2Fjson/examples/example/default-example
  Range: [(90,25)-(90,26)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a should be number
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/%2Ftest%2F%7BitemId%7D/supportedOperation/get/returns/resp/200/payload/application%2Fjson/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json#/web-api/endpoint/%2Ftest%2F%7BitemId%7D/supportedOperation/get/returns/resp/200/payload/application%2Fjson/examples/example/default-example
  Range: [(107,27)-(107,55)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/invalid-examples-params-and-media-type.json
