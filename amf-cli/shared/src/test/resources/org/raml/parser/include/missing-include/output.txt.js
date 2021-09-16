ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/include/missing-include/input.raml
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#syaml-error
  Message: YAML map expected
  Severity: Violation
  Target: 
  Property: 
  Range: [(3,6)-(3,34)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/include/missing-include/input.raml

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/org/raml/parser/include/missing-include/basic-resource.raml'
  Severity: Violation
  Target: basic-resource.raml
  Property: 
  Range: [(3,6)-(3,34)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/include/missing-include/input.raml
