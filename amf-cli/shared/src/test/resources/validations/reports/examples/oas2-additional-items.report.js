ModelId: file://amf-cli/shared/src/test/resources/validations/examples/additional-items.json
Profile: OAS 2.0
Conforms: true
Number of results: 2

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should NOT have more than 2 items
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-boolean/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-boolean/example/default-example
  Range: [(21,17)-(25,7)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/additional-items.json

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [2] should be string
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-object/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/additional-items.json#/declarations/types/array/invalid-tuple-array-additional-items-object/example/default-example
  Range: [(41,17)-(45,7)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/additional-items.json
