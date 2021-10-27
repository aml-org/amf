ModelId: file://amf-cli/shared/src/test/resources/semantic/validation/api.raml
Profile: RAML 1.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/data#file://amf-cli/shared/src/test/resources/semantic/validation/dialect.yaml#/declarations/PaginationAnnotation_pagination_minimum_validation
  Message: Property 'pagination' minimum inclusive value is 1
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/semantic/validation/api.raml#/web-api/endpoint/%2Fsample/supportedOperation/get/returns/resp/201
  Property: http://a.ml/vocab#pagination
  Range: [(10,22)-(10,25)]
  Location: file://amf-cli/shared/src/test/resources/semantic/validation/api.raml

- Constraint: http://a.ml/vocabularies/data#file://amf-cli/shared/src/test/resources/semantic/validation/dialect.yaml#/declarations/PaginationAnnotation_pagination_maximum_validation
  Message: Property 'pagination' maximum inclusive value is 10
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/semantic/validation/api.raml#/web-api/endpoint/%2Fsample/supportedOperation/get/returns/resp/203
  Property: http://a.ml/vocab#pagination
  Range: [(13,22)-(13,24)]
  Location: file://amf-cli/shared/src/test/resources/semantic/validation/api.raml
