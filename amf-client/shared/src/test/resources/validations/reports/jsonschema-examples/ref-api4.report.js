Model: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api4.raml
Profile: RAML
Conforms? false
Number of results: 1

Level: Violation

- Source: http://a.ml/vocabularies/amf/parser#exampleError
  Message: error: instance type (string) does not match any allowed primitive type (allowed: ["integer"])
    level: "error"
    schema: {"loadingURI":"#","pointer":""}
    instance: {"pointer":""}
    domain: "validation"
    keyword: "type"
    found: "string"
    expected: ["integer"]

  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/jsonschema/ref/api4.raml#/web-api/end-points/%2Fep2/get/200/application%2Fjson/scalar/schema/example/default-example
  Property: 
  Position: Some(LexicalInformation([(31,21)-(31,22)]))
  Location: 
