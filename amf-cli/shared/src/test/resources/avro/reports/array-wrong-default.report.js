ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/array-wrong-default.json
Profile: Avro
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: '1' is not a valid value (of type '"string"') for '0'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/array-wrong-default.json#/array/default-array/array_1
  Property: file://amf-cli/shared/src/test/resources/avro/schemas/array-wrong-default.json#/array/default-array/array_1
  Range: [(4,13)-(4,22)]
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/array-wrong-default.json
