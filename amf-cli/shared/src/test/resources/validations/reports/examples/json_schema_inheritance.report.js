ModelId: file://amf-cli/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml
Profile: RAML 1.0
Conforms: true
Number of results: 3

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/parser#json-schema-inheritance
  Message: Inheritance from JSON Schema
  Severity: Warning
  Target: 
  Property: http://a.ml/vocabularies/shapes#inherits
  Range: [(11,0)-(16,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml

- Constraint: http://a.ml/vocabularies/amf/parser#json-schema-inheritance
  Message: Invalid reference to JSON Schema
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml#/declares/array/PersonArray/shape/items
  Property: http://a.ml/vocabularies/shapes#inherits
  Range: [(18,4)-(21,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml

- Constraint: http://a.ml/vocabularies/amf/parser#json-schema-inheritance
  Message: Invalid reference to JSON Schema
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml#/declares/shape/PersonObject/property/property/a/shape/a
  Property: http://a.ml/vocabularies/shapes#inherits
  Range: [(23,4)-(24,29)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml
