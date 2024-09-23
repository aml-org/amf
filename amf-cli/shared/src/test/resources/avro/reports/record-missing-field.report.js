ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/record-missing-field.json
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#invalid-avro-schema
  Message: Internal error during Avro validation: Error: invalid name: "no fields field"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/record-missing-field.json#/shape/no%20fields%20field
  Property: 
  Range: 
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/record-missing-field.json
