ModelId: file://amf-cli/shared/src/test/resources/semantic/validation/api.async.yaml
Profile: ASYNC 2.0
Conforms: false
Number of results: 2

Level: Violation

- Constraint: http://a.ml/vocabularies/data#file://amf-cli/shared/src/test/resources/semantic/validation/dialect.yaml#/declarations/PaginationAnnotation_pagination_minimum_validation
  Message: Property 'pagination' minimum inclusive value is 1
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/semantic/validation/api.async.yaml#/async-api/endpoint/%2Fendpoint1/supportedOperation/subscribe/invalidLowerVal/returns/resp/default-response
  Property: http://a.ml/vocab#pagination
  Range: [(18,22)-(18,25)]
  Location: file://amf-cli/shared/src/test/resources/semantic/validation/api.async.yaml

- Constraint: http://a.ml/vocabularies/data#file://amf-cli/shared/src/test/resources/semantic/validation/dialect.yaml#/declarations/PaginationAnnotation_pagination_maximum_validation
  Message: Property 'pagination' maximum inclusive value is 10
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/semantic/validation/api.async.yaml#/async-api/endpoint/%2Fendpoint2/supportedOperation/subscribe/invalidUpperVal/returns/resp/default-response
  Property: http://a.ml/vocab#pagination
  Range: [(24,22)-(24,24)]
  Location: file://amf-cli/shared/src/test/resources/semantic/validation/api.async.yaml
