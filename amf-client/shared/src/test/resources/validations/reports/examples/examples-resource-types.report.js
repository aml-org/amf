Model: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 4

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-all-params/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/liba.raml

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-example-param/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/liba.raml

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-no-params/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/liba.raml

- Source: http://a.ml/vocabularies/amf/parser#example-validation-error
  Message: should be equal to one of the allowed values
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-type-param/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/liba.raml
