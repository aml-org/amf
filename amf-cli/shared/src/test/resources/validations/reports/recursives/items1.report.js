Model: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml
Profile: RAML 1.0
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: [0].b should be string
[0].c.c should be string
[1].b should be string
[1].c.a[0].b should be string
[1].c.c should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml#/declarations/types/array/A/example/invalid
  Property: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml#/declarations/types/array/A/example/invalid
  Position: Some(LexicalInformation([(18,0)-(26,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: b should be string
c.a[0].b should be string
c.c should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml#/declarations/types/B/example/invalid
  Property: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml#/declarations/types/B/example/invalid
  Position: Some(LexicalInformation([(39,0)-(44,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: a[0].b should be string
a[1].b should be string
a[1].c.c should be string
c should be string

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml#/declarations/types/C/example/invalid
  Property: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml#/declarations/types/C/example/invalid
  Position: Some(LexicalInformation([(57,0)-(63,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/recursives/items1.raml
