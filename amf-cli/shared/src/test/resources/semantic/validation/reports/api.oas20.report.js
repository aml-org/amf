ModelId: file://amf-cli/shared/src/test/resources/semantic/validation/api.oas20.yaml
Profile: OAS 2.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/data#file://amf-cli/shared/src/test/resources/semantic/validation/dialect.yaml#/declarations/PaginationAnnotation_pagination_minimum_validation
  Message: Property 'pagination' minimum inclusive value is 1
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/semantic/validation/api.oas20.yaml#/web-api/endpoint/%2Fendpoint/supportedOperation/get/returns/resp/201
  Property: http://a.ml/vocab#pagination
  Range: [(14,24)-(14,26)]
  Location: file://amf-cli/shared/src/test/resources/semantic/validation/api.oas20.yaml

- Constraint: http://a.ml/vocabularies/data#file://amf-cli/shared/src/test/resources/semantic/validation/dialect.yaml#/declarations/PaginationAnnotation_pagination_maximum_validation
  Message: Property 'pagination' maximum inclusive value is 10
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/semantic/validation/api.oas20.yaml#/web-api/endpoint/%2Fendpoint/supportedOperation/get/returns/resp/203
  Property: http://a.ml/vocab#pagination
  Range: [(17,24)-(17,26)]
  Location: file://amf-cli/shared/src/test/resources/semantic/validation/api.oas20.yaml
