Model: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should have required property 'alive'
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/end-points/%2Fbad0/user/object_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/end-points/%2Fbad0/user/object_1
  Position: Some(LexicalInformation([(22,0)-(23,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: alive should be boolean
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/end-points/%2Fbad1/user/object_1
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml#/web-api/end-points/%2Fbad1/user/object_1
  Position: Some(LexicalInformation([(25,0)-(26,15)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/annotation/object/input.raml
