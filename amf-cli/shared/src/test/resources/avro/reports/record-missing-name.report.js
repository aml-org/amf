ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/record-missing-name.json
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#invalid-avro-schema
  Message: Internal error during Avro validation: Error: missing name property in schema: {"type":"record"}
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/record-missing-name.json#/shape/default-node
  Property: 
  Range: 
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/record-missing-name.json
