Model: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: items should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/customDomainProperties/extension/object_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/customDomainProperties/extension/object_1
  Position: Some(LexicalInformation([(23,0)-(25,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: items should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/customDomainProperties/extension_1/object_1
  Property: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml#/web-api/customDomainProperties/extension_1/object_1
  Position: Some(LexicalInformation([(26,0)-(28,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/annotations/annotations_enum.raml
