Model: file://amf-cli/shared/src/test/resources/org/raml/parser/library/reference-invalid/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'invalidtype'
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/library/reference-invalid/input.raml#/web-api/endpoint/end-points/%2Fitems/supportedOperation/get/resp/210/application%2Fcustom/any/schema/unresolved
  Property: 
  Position: Some(LexicalInformation([(10,20)-(10,31)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/library/reference-invalid/libraries/resource-types.raml
