Model: file://amf-client/shared/src/test/resources/validations/resource_types/non-existent-include.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#declaration-not-found
  Message: ResourceType resourceTypes/nonexistent.raml not found
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(4,15)-(4,54)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/non-existent-include.raml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-client/shared/src/test/resources/validations/resource_types/resourceTypes/nonexistent.raml'
  Level: Violation
  Target: resourceTypes/nonexistent.raml
  Property: 
  Position: Some(LexicalInformation([(4,15)-(4,54)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/non-existent-include.raml
