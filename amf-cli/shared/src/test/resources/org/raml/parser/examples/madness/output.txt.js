ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/madness/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: feathers should NOT be longer than 5 characters
livesInside should be boolean
should have required property 'bite'
should have required property 'claw'
should have required property 'livesOutside'
should match some schema in anyOf

  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/madness/input.raml#/declarations/types/HomeAnimal/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/madness/input.raml#/declarations/types/HomeAnimal/example/default-example
  Range: [(36,0)-(39,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/madness/input.raml
