ModelId: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-type-def.raml
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/missing-includes/conterparty.raml'
  Severity: Violation
  Target: conterparty.raml
  Property: 
  Range: [(6,15)-(6,40)]
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-type-def.raml

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'conterparty.raml'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-type-def.raml#/declarations/types/unresolved
  Property: 
  Range: [(6,15)-(6,40)]
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-type-def.raml
