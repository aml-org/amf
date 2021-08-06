Model: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml
Profile: RAML 0.8
Conforms? true
Number of results: 2

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/endpoint/end-points/%2Fcontacts/supportedOperation/get/returns/200/payload/application%2Fatom%2Bxml/schema/application%2Fatom%2Bxml
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/endpoint/end-points/%2Fcontacts/supportedOperation/get/returns/200/payload/application%2Fatom%2Bxml/any/application%2Fatom%2Bxml/examples/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/examples/contact.xml

- Source: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/endpoint/end-points/%2Fcontacts/supportedOperation/post/returns/201/payload/application%2Fatom%2Bxml/schema/application%2Fatom%2Bxml
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/endpoint/end-points/%2Fcontacts/supportedOperation/post/returns/201/payload/application%2Fatom%2Bxml/any/application%2Fatom%2Bxml/examples/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/examples/contact.xml
