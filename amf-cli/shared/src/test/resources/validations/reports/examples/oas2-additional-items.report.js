Model: file://amf-client/shared/src/test/resources/validations/examples/additional-items.json
Profile: OAS 2.0
Conforms? true
Number of results: 2

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 2 items
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-boolean/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-boolean/example/default-example
  Position: Some(LexicalInformation([(21,17)-(25,7)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/additional-items.json

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [2] should be string
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-object/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-object/example/default-example
  Position: Some(LexicalInformation([(41,17)-(45,7)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/additional-items.json
