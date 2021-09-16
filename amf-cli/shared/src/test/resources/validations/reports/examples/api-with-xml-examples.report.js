ModelId: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml
Profile: RAML 0.8
Conforms: true
Number of results: 2

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/get/200/application%2Fatom%2Bxml/schema/application%2Fatom%2Bxml
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/get/200/application%2Fatom%2Bxml/any/application%2Fatom%2Bxml/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Range: [(1,0)-(1,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/examples/contact.xml

- Constraint: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/post/201/application%2Fatom%2Bxml/schema/application%2Fatom%2Bxml
  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/api.raml#/web-api/end-points/%2Fcontacts/post/201/application%2Fatom%2Bxml/any/application%2Fatom%2Bxml/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Range: [(1,0)-(1,0)]
  Location: file://amf-cli/shared/src/test/resources/validations/api-with-xml-examples/examples/contact.xml
