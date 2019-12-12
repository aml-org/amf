Model: file://amf-client/shared/src/test/resources/validations/japanese/oas/facet-string-length-invalid.yaml
Profile: OAS 2.0
Conforms? true
Number of results: 2

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: expected maxLength: 3, actual: 8
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/japanese/oas/facet-string-length-invalid.yaml#/declarations/types/scalar/SomeObj/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/japanese/oas/facet-string-length-invalid.yaml#/declarations/types/scalar/SomeObj/example/default-example
  Position: Some(LexicalInformation([(29,15)-(29,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/japanese/oas/facet-string-length-invalid.yaml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: expected maxLength: 3, actual: 8
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/japanese/oas/facet-string-length-invalid.yaml#/web-api/end-points/%2Fping/post/request/application%2Fjson/scalar/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/japanese/oas/facet-string-length-invalid.yaml#/web-api/end-points/%2Fping/post/request/application%2Fjson/scalar/schema/example/default-example
  Position: Some(LexicalInformation([(29,15)-(29,23)]))
  Location: file://amf-client/shared/src/test/resources/validations/japanese/oas/facet-string-length-invalid.yaml