ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/library/reference-invalid/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'invalidtype'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/library/reference-invalid/input.raml#/web-api/end-points/%2Fitems/get/210/application%2Fcustom/any/schema/unresolved
  Property: 
  Range: [(10,20)-(10,31)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/library/reference-invalid/libraries/resource-types.raml
