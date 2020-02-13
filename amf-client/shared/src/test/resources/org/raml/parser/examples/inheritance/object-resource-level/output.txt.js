Model: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml
Profile: RAML 1.0
Conforms? false
Number of results: 7

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exclusive-schemas-type
  Message: 'schemas' and 'types' properties are mutually exclusive
  Level: Violation
  Target: 
  Property: 
  Position: Some(LexicalInformation([(5,0)-(5,7)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/core#unresolved-reference
  Message: Unresolved reference 'Player'
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml#/web-api/end-points/%2Fusers/get/202/application%2Fjson/any/schema/unresolved
  Property: 
  Position: Some(LexicalInformation([(89,20)-(89,26)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

Level: Warning

- Source: http://a.ml/vocabularies/amf/parser#schemas-deprecated
  Message: 'schemas' keyword it's deprecated for 1.0 version, should use 'types' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(5,0)-(5,7)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(85,12)-(85,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(89,12)-(89,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(93,12)-(93,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(110,12)-(110,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml
