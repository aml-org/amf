Model: file://amf-client/shared/src/test/resources/validations/examples/number-prop-bool-example.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: file://amf-client/shared/src/test/resources/validations/examples/number-prop-bool-example.raml#/web-api/end-points/%2Fteams/get/request/application%2Fxml/schema/type_validation
  Message: Object at / must be valid
Scalar at //prop2 must have data type http://a.ml/vocabularies/shapes#number

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/number-prop-bool-example.raml#/web-api/end-points/%2Fteams/get/request/application%2Fxml/schema/example/default-example
  Property: http://a.ml/vocabularies/data#prop2
  Position: Some(LexicalInformation([(16,0)-(20,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/number-prop-bool-example.raml
