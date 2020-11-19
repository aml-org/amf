Model: file://amf-client/shared/src/test/resources/validations/api-with-xml-examples/api.raml
Profile: RAML 0.8
Conforms? true
Number of results: 2

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-client/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/get/200/application%2Fatom%2Bxml/schema/application%2Fatom%2Bxml
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/get/200/application%2Fatom%2Bxml/any/application%2Fatom%2Bxml/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(28,21)-(28,50)]))
  Location: file://amf-client/shared/src/test/resources/validations/api-with-xml-examples/api.raml

- Source: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-client/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/post/201/application%2Fatom%2Bxml/schema/application%2Fatom%2Bxml
  Level: Warning
  Target: file://amf-client/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/post/201/application%2Fatom%2Bxml/any/application%2Fatom%2Bxml/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(34,21)-(34,50)]))
  Location: file://amf-client/shared/src/test/resources/validations/api-with-xml-examples/api.raml
