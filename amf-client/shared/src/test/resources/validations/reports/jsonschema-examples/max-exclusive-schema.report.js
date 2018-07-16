Model: file://amf-client/shared/src/test/resources/validations/jsonschema/max-exclusive-schema.raml
Profile: RAML08
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"exclusiveMaximum","dataPath":"","schemaPath":"#/exclusiveMaximum","params":{"comparison":"<","limit":180,"exclusive":true},"message":"should be < 180"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/max-exclusive-schema.raml#/declarations/schemas/invalidExample/property/prop1/scalar/prop1/example/default-example
  Property: 
  Position: Some(LexicalInformation([(20,36)-(20,39)]))
  Location: file://amf-client/shared/src/test/resources/validations/jsonschema/max-exclusive-schema.raml
