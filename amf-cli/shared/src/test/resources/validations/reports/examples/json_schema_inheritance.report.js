Model: file://amf-client/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml
Profile: RAML 1.0
Conforms? true
Number of results: 3

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#json-schema-inheritance
  Message: Inheritance from JSON Schema
  Level: Warning
  Target: 
  Property: http://a.ml/vocabularies/shapes#inherits
  Position: Some(LexicalInformation([(11,0)-(16,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml

- Source: http://a.ml/vocabularies/amf/parser#json-schema-inheritance
  Message: Invalid reference to JSON Schema
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml#/declarations/types/Person/schema
  Property: http://a.ml/vocabularies/shapes#inherits
  Position: Some(LexicalInformation([(18,4)-(21,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml

- Source: http://a.ml/vocabularies/amf/parser#json-schema-inheritance
  Message: Invalid reference to JSON Schema
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml#/declarations/types/PersonObject/property/a/a
  Property: http://a.ml/vocabularies/shapes#inherits
  Position: Some(LexicalInformation([(23,4)-(24,29)]))
  Location: file://amf-client/shared/src/test/resources/validations/production/json_schema_inheritance/api.raml
