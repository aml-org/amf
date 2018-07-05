Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml
Profile: RAML
Conforms? false
Number of results: 2

Level: Violation

- Source: file://amf-client/shared/src/test/resources/org/raml/parser/examples/music-api/songs-library.raml#/declarations/types/Musician_validation
  Message: Object at / must be valid
Array items at //discography must be valid

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/music-api/songs-library.raml#/declarations/types/Musician/example/default-example
  Property: http://a.ml/vocabularies/data#discography
  Position: Some(LexicalInformation([(20,0)-(28,0)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-client/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml#/web-api/end-points/%2Fsongs%2F%7BsongId%7D/get/200/application%2Fxml/schema/schema
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml#/web-api/end-points/%2Fsongs%2F%7BsongId%7D/get/200/application%2Fxml/schema/schema/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(37,23)-(37,50)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml
