ModelId: file://amf-cli/shared/src/test/resources/validations/resource_types/nested-endpoint.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/resolution#nested-endpoint
  Message: Nested endpoint in resourceType: '/groups'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/resource_types/nested-endpoint.raml#/web-api/end-points/%2Fusers/hasGroups
  Property: 
  Range: [(9,4)-(9,11)]
  Location: file://amf-cli/shared/src/test/resources/validations/resource_types/nested-endpoint.raml
