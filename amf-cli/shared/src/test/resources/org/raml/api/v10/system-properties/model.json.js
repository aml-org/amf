ModelId: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: startDate should match format "date"
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml#/web-api/end-points/%2Fsubscription/post/request/application%2Fjson/application%2Fjson/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml#/web-api/end-points/%2Fsubscription/post/request/application%2Fjson/application%2Fjson/example/default-example
  Range: [(1,0)-(4,1)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/example.json

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Severity: Warning
  Target: 
  Property: 
  Range: [(9,8)-(9,14)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/api/v10/system-properties/input.raml
