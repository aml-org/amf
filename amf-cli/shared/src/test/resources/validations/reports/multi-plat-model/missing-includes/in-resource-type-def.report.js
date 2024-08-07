ModelId: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-resource-type-def.raml
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#declaration-not-found
  Message: ResourceType resourceTypes/idReturned.raml not found
  Severity: Violation
  Target: 
  Property: 
  Range: [(5,16)-(5,54)]
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-resource-type-def.raml

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/missing-includes/resourceTypes/idReturned.raml'
  Severity: Violation
  Target: resourceTypes/idReturned.raml
  Property: 
  Range: [(5,16)-(5,54)]
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-resource-type-def.raml
