Model: file://amf-cli/shared/src/test/resources/validations/oas3/additional-items.json
Profile: OAS 3.0
Conforms? true
Number of results: 2

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 2 items
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/additional-items.json#/declares/array/invalid-tuple-array-additional-items-boolean/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/additional-items.json#/declares/array/invalid-tuple-array-additional-items-boolean/examples/example/default-example
  Position: Some(LexicalInformation([(21,19)-(25,9)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/additional-items.json

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [2] should be string
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/oas3/additional-items.json#/declares/array/invalid-tuple-array-additional-items-object/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/oas3/additional-items.json#/declares/array/invalid-tuple-array-additional-items-object/examples/example/default-example
  Position: Some(LexicalInformation([(41,19)-(45,9)]))
  Location: file://amf-cli/shared/src/test/resources/validations/oas3/additional-items.json
