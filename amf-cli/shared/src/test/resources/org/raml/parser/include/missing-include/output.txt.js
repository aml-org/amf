Model: file://amf-client/shared/src/test/resources/org/raml/parser/include/missing-include/input.raml
Profile: 
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#syaml-error
  Message: YAML map expected
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(3,6)-(3,34)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/include/missing-include/input.raml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-client/shared/src/test/resources/org/raml/parser/include/missing-include/basic-resource.raml'
  Level: Violation
  Target: basic-resource.raml
  Property: 
  Position: Some(LexicalInformation([(3,6)-(3,34)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/include/missing-include/input.raml
