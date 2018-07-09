Model: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: error: instance type (integer) does not match any allowed primitive type (allowed: ["string"])
    level: "error"
    schema: {"loadingURI":"#","pointer":"/properties/lastName"}
    instance: {"pointer":"/lastName"}
    domain: "validation"
    keyword: "type"
    found: "integer"
    expected: ["string"]

error: instance type (integer) does not match any allowed primitive type (allowed: ["string"])
    level: "error"
    schema: {"loadingURI":"#","pointer":"/properties/name"}
    instance: {"pointer":"/name"}
    domain: "validation"
    keyword: "type"
    found: "integer"
    expected: ["string"]


  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref.raml#/declarations/types/Person/example/default-example
  Property: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref.raml#/declarations/types/Person/example/default-example
  Position: Some(LexicalInformation([(10,0)-(13,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/examples/declared-type-ref.raml
