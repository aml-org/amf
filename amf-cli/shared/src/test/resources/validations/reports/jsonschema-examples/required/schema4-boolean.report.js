Model: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema4-boolean.raml
Profile: 
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#invalid-required-value
  Message: 'required' field has to be an array
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema4-boolean.raml#/web-api/end-points/%2Fproducts/put/request/application%2Fjson/application%2Fjson
  Property: 
  Position: Some(LexicalInformation([(17,17)-(17,33)]))
  Location: file://amf-cli/shared/src/test/resources/validations/jsonschema/required/schema4-boolean.raml
