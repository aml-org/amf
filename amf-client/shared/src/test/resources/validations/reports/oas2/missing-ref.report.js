Model: file://amf-client/shared/src/test/resources/validations/oas2/missing-ref.yaml
Profile: OAS 2.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'ref.yaml#/definitions/person'
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/oas2/missing-ref.yaml#/declarations/types/person/property/sibling/unresolved
  Property: 
  Position: Some(LexicalInformation([(13,8)-(13,44)]))
  Location: file://amf-client/shared/src/test/resources/validations/oas2/missing-ref.yaml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-client/shared/src/test/resources/validations/oas2/ref.yaml'
  Level: Violation
  Target: ref.yaml
  Property: 
  Position: Some(LexicalInformation([(13,14)-(13,44)]))
  Location: file://amf-client/shared/src/test/resources/validations/oas2/missing-ref.yaml
