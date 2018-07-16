Model: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml
Profile: RAML
Conforms? false
Number of results: 4

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"enum","dataPath":"","schemaPath":"#/enum","params":{"allowedValues":["yogi","pooh"]},"message":"should be equal to one of the allowed values"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-all-params/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"enum","dataPath":"","schemaPath":"#/enum","params":{"allowedValues":["yogi","pooh"]},"message":"should be equal to one of the allowed values"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-example-param/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"enum","dataPath":"","schemaPath":"#/enum","params":{"allowedValues":["yogi","pooh"]},"message":"should be equal to one of the allowed values"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-no-params/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"enum","dataPath":"","schemaPath":"#/enum","params":{"allowedValues":["yogi","pooh"]},"message":"should be equal to one of the allowed values"}
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml#/web-api/end-points/%2Fuser/post/request/parameter/broken-type-param/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/resource_types/parameterized-references/input.raml
