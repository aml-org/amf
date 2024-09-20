ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/map-wrong-default.json
Profile: Avro
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: 'wrong default' is not a valid value (of type '{"type":"map","values":"long"}') for ''
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/map-wrong-default.json#/shape/default-node/scalar_1
  Property: file://amf-cli/shared/src/test/resources/avro/schemas/map-wrong-default.json#/shape/default-node/scalar_1
  Range: [(4,13)-(4,28)]
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/map-wrong-default.json
