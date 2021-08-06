Model: file://amf-cli/shared/src/test/resources/validations/xmlexample/offices_xml_type.raml
Profile: RAML 1.0
Conforms? true
Number of results: 2

Level: Warning

- Source: http://a.ml/vocabularies/amf/validation#unsupported-example-media-type-warning
  Message: Unsupported validation for mediatype: application/xml and shape file://amf-cli/shared/src/test/resources/validations/xmlexample/offices_xml_type.raml#/declares/schema/Office
  Level: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/xmlexample/offices_xml_type.raml#/declares/schema/Office/examples/example/default-example
  Property: http://a.ml/vocabularies/document#value
  Position: Some(LexicalInformation([(1,0)-(1,0)]))
  Location: file://amf-cli/shared/src/test/resources/validations/xmlexample/office_example.xml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(8,8)-(8,14)]))
  Location: file://amf-cli/shared/src/test/resources/validations/xmlexample/offices_xml_type.raml
