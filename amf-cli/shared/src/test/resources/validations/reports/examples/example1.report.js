ModelId: file://amf-cli/shared/src/test/resources/validations/examples/example1.raml
Profile: RAML 1.0
Conforms: true
Number of results: 1

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Severity: Warning
  Target: 
  Property: 
  Range: [(34,12)-(34,18)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/example1.raml
