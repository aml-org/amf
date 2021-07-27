Model: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml
Profile: RAML 1.0
Conforms? false
Number of results: 4

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be integer
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int/examples/example/invalid
  Position: Some(LexicalInformation([(10,15)-(10,19)]))
  Location: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 127
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int8/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int8/examples/example/invalid
  Position: Some(LexicalInformation([(16,15)-(16,18)]))
  Location: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 32767
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int16/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int16/examples/example/invalid
  Position: Some(LexicalInformation([(22,15)-(22,20)]))
  Location: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be <= 2147483647
  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int32/examples/example/invalid
  Property: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml/declares/scalar/int32/examples/example/invalid
  Position: Some(LexicalInformation([(28,15)-(28,25)]))
  Location: file://amf-cli/shared/src/test/resources/validations/types/int-formats.raml
