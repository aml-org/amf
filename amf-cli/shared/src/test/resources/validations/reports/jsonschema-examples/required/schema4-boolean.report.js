Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema4-boolean.raml
Profile: 
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#invalid-required-value
  Message: 'required' field has to be an array
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema4-boolean.raml#/web-api/endpoint/%2Fproducts/supportedOperation/put/expects/request/payload/application%2Fjson/shape/application%2Fjson
  Property: 
  Position: Some(LexicalInformation([(17,17)-(17,33)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema4-boolean.raml
