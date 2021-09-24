ModelId: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml
Profile: RAML 1.0
Conforms: false
Number of results: 4

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/customDomainProperties/extension/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/customDomainProperties/extension/scalar_1
  Range: [(6,20)-(6,21)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/endpoint/%2Fresource/customDomainProperties/extension/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/endpoint/%2Fresource/customDomainProperties/extension/scalar_1
  Range: [(10,22)-(10,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/endpoint/%2Fresource/name/extension/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/endpoint/%2Fresource/name/extension/scalar_1
  Range: [(14,24)-(14,25)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/endpoint/%2Fresource/description/extension/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml#/web-api/endpoint/%2Fresource/description/extension/scalar_1
  Range: [(18,24)-(18,25)]
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotating-scalar-valued-nodes.raml
