Model: file://amf-client/shared/src/test/resources/pce/invalid-schema-dependencies.raml
Profile: RAML 1.0
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: B should NOT be shorter than 3 characters
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/pce/invalid-schema-dependencies.raml#/web-api/end-points/%2Ffoo/post/request/application%2Fjson/schema/example/default-example
  Property: file://amf-client/shared/src/test/resources/pce/invalid-schema-dependencies.raml#/web-api/end-points/%2Ffoo/post/request/application%2Fjson/schema/example/default-example
  Position: Some(LexicalInformation([(36,16)-(38,17)]))
  Location: file://amf-client/shared/src/test/resources/pce/invalid-schema-dependencies.raml
