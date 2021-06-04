Model: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-trait-def.raml
Profile: 
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#declaration-not-found
  Message: Trait traits/nonExists.raml not found
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(5,15)-(5,45)]))
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-trait-def.raml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/missing-includes/traits/nonExists.raml'
  Level: Violation
  Target: traits/nonExists.raml
  Property: 
  Position: Some(LexicalInformation([(5,15)-(5,45)]))
  Location: file://amf-cli/shared/src/test/resources/validations/missing-includes/in-trait-def.raml
