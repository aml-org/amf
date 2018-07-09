Model: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml
Profile: RAML
Conforms? false
Number of results: 3

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: error: instance type (integer) does not match any allowed primitive type (allowed: ["string"])
    level: "error"
    schema: {"loadingURI":"#","pointer":""}
    instance: {"pointer":""}
    domain: "validation"
    keyword: "type"
    found: "integer"
    expected: ["string"]

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/image/scalar_1
  Property:
  Position: Some(LexicalInformation([(18,11)-(18,12)]))
  Location:

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: error: string "Bar" is too long (length: 3, maximum allowed: 2)
    level: "error"
    schema: {"loadingURI":"#","pointer":""}
    instance: {"pointer":""}
    domain: "validation"
    keyword: "maxLength"
    value: "Bar"
    found: 3
    maxLength: 2

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/foo/scalar_1
  Property:
  Position: Some(LexicalInformation([(19,9)-(19,12)]))
  Location:

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: error: string "bores" is too short (length: 5, required minimum: 10)
    level: "error"
    schema: {"loadingURI":"#","pointer":""}
    instance: {"pointer":""}
    domain: "validation"
    keyword: "minLength"
    value: "bores"
    found: 5
    minLength: 10

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/org/raml/parser/annotation/string/input.raml#/web-api/end-points/%2Ftext/tato/scalar_1
  Property:
  Position: Some(LexicalInformation([(20,10)-(20,15)]))
  Location:
