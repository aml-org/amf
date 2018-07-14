Model: file://amf-client/shared/src/test/resources/validations/types/lengths.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: {"keyword":"maxLength","dataPath":".lastname","schemaPath":"#/properties/lastname/maxLength","params":{"limit":5},"message":"should NOT be longer than 5 characters"}
{"keyword":"minLength","dataPath":".name","schemaPath":"#/properties/name/minLength","params":{"limit":5},"message":"should NOT be shorter than 5 characters"}

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/types/lengths.raml#/declarations/types/User/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/types/lengths.raml#/declarations/types/User/example/default-example
  Position: Some(LexicalInformation([(15,0)-(16,26)]))
  Location: file://amf-client/shared/src/test/resources/validations/types/lengths.raml
