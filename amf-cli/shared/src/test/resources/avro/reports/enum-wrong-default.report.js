ModelId: file://amf-cli/shared/src/test/resources/avro/schemas/enum-wrong-default.json
Profile: Avro
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: 'WRONG' is not a valid value (of type '"Suit"') for ''
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/avro/schemas/enum-wrong-default.json#/scalar/Suit/scalar_1
  Property: file://amf-cli/shared/src/test/resources/avro/schemas/enum-wrong-default.json#/scalar/Suit/scalar_1
  Range: [(10,13)-(10,20)]
  Location: file://amf-cli/shared/src/test/resources/avro/schemas/enum-wrong-default.json
