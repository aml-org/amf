Model: file://amf-cli/shared/src/test/resources/validations/extends/references/nonExistentIncludes.raml
Profile: 
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/extends/references/nonExitentFile.raml'
  Level: Violation
  Target: nonExitentFile.raml
  Property: 
  Position: Some(LexicalInformation([(8,14)-(8,42)]))
  Location: file://amf-cli/shared/src/test/resources/validations/extends/references/nonExistentIncludes.raml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/extends/references/nonExistentFile.raml'
  Level: Violation
  Target: nonExistentFile.raml
  Property: 
  Position: Some(LexicalInformation([(18,17)-(18,46)]))
  Location: file://amf-cli/shared/src/test/resources/validations/extends/references/nonExistentIncludes.raml
