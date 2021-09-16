ModelId: file://amf-cli/shared/src/test/resources/validations/oas2/missing-ref.yaml
Profile: 
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'ref.yaml#/definitions/person'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/oas2/missing-ref.yaml#/declarations/types/person/property/sibling/unresolved
  Property: 
  Range: [(13,8)-(13,44)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas2/missing-ref.yaml

- Constraint: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: File Not Found: ENOENT: no such file or directory, open 'amf-cli/shared/src/test/resources/validations/oas2/ref.yaml'
  Severity: Violation
  Target: ref.yaml
  Property: 
  Range: [(13,14)-(13,44)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas2/missing-ref.yaml
