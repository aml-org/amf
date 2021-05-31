Model: file://amf-client/shared/src/test/resources/validations/conversion-exceptions/invalid-graph-dependency-value.jsonld
Profile: 
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#syaml-error
  Message: Expecting !!str, !!seq provided
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(9,15)-(9,56)]))
  Location: file://amf-client/shared/src/test/resources/validations/conversion-exceptions/invalid-graph-dependency-value.jsonld

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: EISDIR: illegal operation on a directory, read
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(9,15)-(9,56)]))
  Location: file://amf-client/shared/src/test/resources/validations/conversion-exceptions/invalid-graph-dependency-value.jsonld
