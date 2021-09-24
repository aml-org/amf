ModelId: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties4.raml
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/parser#pattern-properties-on-closed-node
  Message: Node without additional properties support cannot have pattern properties
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties4.raml#/declares/shape/Person
  Property: 
  Range: [(4,9)-(11,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties4.raml
