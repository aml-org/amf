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
  Position: Some(LexicalInformation([(87,20)-(87,26)]))
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
  Position: Some(LexicalInformation([(83,12)-(83,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(87,12)-(87,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(91,12)-(91,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml

- Source: http://a.ml/vocabularies/amf/parser#schema-deprecated
  Message: 'schema' keyword it's deprecated for 1.0 version, should use 'type' instead
  Level: Warning
  Target: 
  Property: 
  Position: Some(LexicalInformation([(107,12)-(107,18)]))
  Location: file://amf-client/shared/src/test/resources/org/raml/parser/examples/inheritance/object-resource-level/input.raml
