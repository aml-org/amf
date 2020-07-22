Model: file://amf-client/shared/src/test/resources/validations/mercury.raml
Profile: mercury
Conforms? false
Number of results: 5

Level: Violation

- Source: http://a.ml/vocabularies/data#camelcase-query-parameters
  Message: Query parameters names must be in camelcase
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/mercury.raml#/web-api/end-points/%2Fresource%2F%7Bresource_id%7D/get/request/parameter/expand_param
  Property: 
  Position: Some(LexicalInformation([(32,16)-(39,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/mercury.raml

- Source: http://a.ml/vocabularies/data#no-todo-text-in-description-fields
  Message: Empty string or The word 'TODO' (case insensitive) is not allowed in any description field.
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/mercury.raml#/web-api/end-points/%2Fresource%2F%7Bresource_id%7D/post
  Property: 
  Position: Some(LexicalInformation([(55,8)-(63,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/mercury.raml

- Source: http://a.ml/vocabularies/data#upper-camelcase-datatype
  Message: Data type definitions should be UpperCamelCase
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/mercury.raml
  Property: 
  Position: Some(LexicalInformation([(0,0)-(0,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/mercury.raml

- Source: http://a.ml/vocabularies/data#upper-camelcase-datatype
  Message: Data type definitions should be UpperCamelCase
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/mercury.raml
  Property: 
  Position: Some(LexicalInformation([(0,0)-(0,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/mercury.raml

- Source: http://a.ml/vocabularies/data#at-least-one-2xx-or-3xx-response
  Message: Methods must have at least one 2xx or 3xx response
  Level: Violation
  Target: file://amf-client/shared/src/test/resources/validations/mercury.raml#/web-api/end-points/%2Fresource%2F%7Bresource_id%7D/put
  Property: http://a.ml/vocabularies/apiContract#returns
  Position: Some(LexicalInformation([(66,12)-(69,0)]))
  Location: file://amf-client/shared/src/test/resources/validations/mercury.raml
