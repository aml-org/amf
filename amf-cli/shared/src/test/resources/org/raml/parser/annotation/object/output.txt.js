ModelId: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'alive'
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/endpoint/%2Fbad0/customDomainProperties/user/object_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/endpoint/%2Fbad0/customDomainProperties/user/object_1
  Range: [(22,0)-(23,0)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: alive should be boolean
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/endpoint/%2Fbad1/customDomainProperties/user/object_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/endpoint/%2Fbad1/customDomainProperties/user/object_1
  Range: [(25,0)-(26,15)]
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml
