ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/types/inexistent-single-inheritance-type-properties/input.raml
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'InexistentType'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/types/inexistent-single-inheritance-type-properties/input.raml#/declares/shape/MyType/inherits/unresolved
  Property: 
  Range: [(5,10)-(5,24)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/types/inexistent-single-inheritance-type-properties/input.raml
