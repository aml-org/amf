Model: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml
Profile: OAS 3.0
Conforms? true
Number of results: 1

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should match pattern "^([\u30a0-\u30ff]+)"
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml#/declarations/types/scalar/SomeObj/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml#/declarations/types/scalar/SomeObj/example/default-example
  Position: Some(LexicalInformation([(28,15)-(28,23)]))
  Location: file://amf-cli/shared/src/test/resources/validations/japanese/oas/facet-string-pattern-invalid.yaml
