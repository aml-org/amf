ModelId: file://amf-cli/shared/src/test/resources/validations/conversion-exceptions/invalid-graph-dependency-value.jsonld
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#syaml-error
  Message: Expecting !!str, !!seq provided
  Severity: Violation
  Target: 
  Property: 
  Range: [(9,15)-(9,56)]
  Location: file://amf-cli/shared/src/test/resources/validations/conversion-exceptions/invalid-graph-dependency-value.jsonld

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: EISDIR: illegal operation on a directory, read
  Severity: Violation
  Target: 
  Property: 
  Range: [(9,15)-(9,56)]
  Location: file://amf-cli/shared/src/test/resources/validations/conversion-exceptions/invalid-graph-dependency-value.jsonld
