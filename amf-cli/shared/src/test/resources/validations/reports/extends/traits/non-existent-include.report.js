Model: file://amf-cli/shared/src/test/resources/validations/traits/non-existent-include.raml
Profile: 
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#declaration-not-found
  Message: Trait traits/nonexistent.raml not found
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(4,15)-(4,47)]))
  Location: file://amf-cli/shared/src/test/resources/validations/traits/non-existent-include.raml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/traits/traits/nonexistent.raml'
  Level: Violation
  Target: traits/nonexistent.raml
  Property: 
  Position: Some(LexicalInformation([(4,15)-(4,47)]))
  Location: file://amf-cli/shared/src/test/resources/validations/traits/non-existent-include.raml