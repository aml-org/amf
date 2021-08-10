Model: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 2

Level: Violation

- Source: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: discography[0] should have required property 'songs'
discography[0] should match some schema in anyOf
discography[0].length should be multiple of 100

  Level: Violation
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml#/references/0/declares/shape/Musician/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml#/references/0/declares/shape/Musician/examples/example/default-example
  Position: Some(LexicalInformation([(20,0)-(28,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/music-api/songs-library.raml

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml#/web-api/endpoint/end-points/%2Fsongs%2F%7BsongId%7D/supportedOperation/get/returns/resp/200/payload/application%2Fxml/schema/schema
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/music-api/input.raml#/web-api/endpoint/end-points/%2Fsongs%2F%7BsongId%7D/supportedOperation/get/returns/resp/200/payload/application%2Fxml/schema/schema/examples/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-cli/shared/src/test/resources/org/raml/parser/examples/music-api/examples/songs.xml
