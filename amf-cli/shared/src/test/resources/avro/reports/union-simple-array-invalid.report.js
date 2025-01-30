ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/union-simple-array-invalid.json
Profile: Avro
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: '132' is not a valid value (of type '"string"') for '0.string'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/union-simple-array-invalid.json#/array/default-array/array_1
  Property: file://amf-cli/shared/src/test/resources/avro/schemas/union-simple-array-invalid.json#/array/default-array/array_1
  Range: [(4,13)-(4,49)]
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/union-simple-array-invalid.json
