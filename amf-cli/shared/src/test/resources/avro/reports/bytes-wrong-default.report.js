ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/bytes-wrong-default.json
Profile: Avro
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: 'wrong default type' is not a valid value (of type '"bytes"') for ''
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/bytes-wrong-default.json#/shape/test/property/property/bytesPrimitiveType/scalar/default-scalar/scalar_1
  Property: file://amf-cli/shared/src/test/resources/avro/schemas/bytes-wrong-default.json#/shape/test/property/property/bytesPrimitiveType/scalar/default-scalar/scalar_1
  Range: [(8,17)-(8,37)]
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/bytes-wrong-default.json
