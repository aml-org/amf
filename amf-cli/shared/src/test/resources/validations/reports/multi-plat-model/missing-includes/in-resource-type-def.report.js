Model: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-resource-type-def.raml
Profile: 
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#declaration-not-found
  Message: ResourceType resourceTypes/idReturned.raml not found
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(5,16)-(5,54)]))
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-resource-type-def.raml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/missing-includes/resourceTypes/idReturned.raml'
  Level: Violation
  Target: resourceTypes/idReturned.raml
  Property: 
  Position: Some(LexicalInformation([(5,16)-(5,54)]))
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-resource-type-def.raml
