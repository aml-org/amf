ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/union-root-invalid.json
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#invalid-avro-schema
  Message: Internal error during Avro validation: Error: unknown type: ["string","int"]
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/union-root-invalid.json#/union/simpleUnion
  Property: 
  Range: 
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/union-root-invalid.json
