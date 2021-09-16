ModelId: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be integer
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml#/web-api/end-points/%2FUsuario/delete/request/application%2Fjson/application%2Fjson/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml#/web-api/end-points/%2FUsuario/delete/request/application%2Fjson/application%2Fjson/example/default-example
  Range: [(22,16)-(24,17)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Severity: Warning
  Target: 
  Property: 
  Range: [(11,8)-(11,14)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml

- Constraint: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Severity: Warning
  Target: 
  Property: 
  Range: [(20,8)-(20,14)]
  Location: file://amf-cli/shared/src/test/resources/validations/production/null-keys/api.raml
