Model: file://amf-client/shared/src/test/resources/validations/examples/string-hierarchy.raml
Profile: RAML
Conforms? false
Number of results: 4

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
  Target: file://amf-client/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declarations/types/scalar/SomeType/example/validInt1
  Property:
  Position: Some(LexicalInformation([(11,17)-(11,18)]))
  Location:

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
  Target: file://amf-client/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declarations/types/scalar/SomeType/example/validInt2
  Property:
  Position: Some(LexicalInformation([(12,17)-(12,29)]))
  Location:

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: error: instance type (boolean) does not match any allowed primitive type (allowed: ["string"])
    level: "error"
    schema: {"loadingURI":"#","pointer":""}
    instance: {"pointer":""}
    domain: "validation"
    keyword: "type"
    found: "boolean"
    expected: ["string"]

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declarations/types/scalar/SomeType/example/validBoolean
  Property:
  Position: Some(LexicalInformation([(13,20)-(13,24)]))
  Location:

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: error: instance type (number) does not match any allowed primitive type (allowed: ["string"])
    level: "error"
    schema: {"loadingURI":"#","pointer":""}
    instance: {"pointer":""}
    domain: "validation"
    keyword: "type"
    found: "number"
    expected: ["string"]

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/string-hierarchy.raml#/declarations/types/scalar/SomeType/example/validNumber
  Property:
  Position: Some(LexicalInformation([(14,19)-(14,22)]))
  Location:
