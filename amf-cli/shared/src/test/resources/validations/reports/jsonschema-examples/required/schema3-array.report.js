ModelId: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema3-array.raml
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#invalid-required-array-for-schema-version
  Message: Required arrays of properties not supported in JSON Schema below version draft-4
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema3-array.raml#/web-api/endpoint/%2Fproducts/supportedOperation/put/expects/request/payload/application%2Fjson/shape/application%2Fjson
  Property: 
  Range: [(17,29)-(17,40)]
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema3-array.raml
