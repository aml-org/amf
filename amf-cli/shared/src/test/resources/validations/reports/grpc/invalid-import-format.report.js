ModelId: file://amf-cli/shared/src/test/resources/validations/grpc/invalid-import-format.proto
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#antlr-error
  Message: <missing ';'>
  Severity: Violation
  Target: 
  Property: 
  Range: [(5,7)-(5,8)]
  Location: file://amf-cli/shared/src/test/resources/validations/grpc/invalid-import-format.proto

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: EISDIR: illegal operation on a directory, read
  Severity: Violation
  Target: 
  Property: 
  Range: [(6,0)-(5,13)]
  Location: file://amf-cli/shared/src/test/resources/validations/grpc/invalid-import-format.proto
