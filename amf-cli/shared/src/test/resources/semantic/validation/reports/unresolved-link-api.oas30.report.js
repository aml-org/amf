ModelId: file://amf-cli/shared/src/test/resources/semantic/validation/unresolved-link-api.oas30.yaml
Profile: 
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'something'
  Severity: Violation
  Target: nonImportantId/x-maintainer/users/0
  Property: 
  Range: [(7,9)-(7,18)]
  Location: file://amf-cli/shared/src/test/resources/semantic/validation/unresolved-link-api.oas30.yaml
