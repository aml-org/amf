ModelId: file://amf-cli/shared/src/test/resources/validations/extends/references/nonExistentIncludes.raml
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/extends/references/nonExitentFile.raml'
  Severity: Violation
  Target: nonExitentFile.raml
  Property: 
  Range: [(8,14)-(8,42)]
  Location: file://amf-cli/shared/src/test/resources/validations/extends/references/nonExistentIncludes.raml

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/extends/references/nonExistentFile.raml'
  Severity: Violation
  Target: nonExistentFile.raml
  Property: 
  Range: [(18,17)-(18,46)]
  Location: file://amf-cli/shared/src/test/resources/validations/extends/references/nonExistentIncludes.raml
