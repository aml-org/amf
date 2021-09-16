ModelId: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-trait-def.raml
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#declaration-not-found
  Message: Trait traits/nonExists.raml not found
  Severity: Violation
  Target: 
  Property: 
  Range: [(5,15)-(5,45)]
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-trait-def.raml

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/missing-includes/traits/nonExists.raml'
  Severity: Violation
  Target: traits/nonExists.raml
  Property: 
  Range: [(5,15)-(5,45)]
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-trait-def.raml
