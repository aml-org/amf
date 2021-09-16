ModelId: file://amf-cli/shared/src/test/resources/validations/examples/invalid-json-schema-version-properties.raml
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#invalid-required-array-for-schema-version
  Message: Required arrays of properties not supported in JSON Schema below version draft-4
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/invalid-json-schema-version-properties.raml#/declarations/schemas/Request
  Property: 
  Range: [(16,26)-(16,31)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/invalid-json-schema-version-properties.raml
