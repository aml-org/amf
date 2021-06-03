Model: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties4.raml
Profile: 
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#pattern-properties-on-closed-node
  Message: Node without additional properties support cannot have pattern properties
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties4.raml#/declarations/types/Person
  Property: 
  Position: Some(LexicalInformation([(4,9)-(11,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/pattern-properties/pattern_properties4.raml
