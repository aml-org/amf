ModelId: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml
Profile: RAML 1.0
Conforms: false
Number of results: 3

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml#/web-api/endpoint/%2Fpsn/supportedOperation/head/returns/resp/204/header/parameter/header/X-PSN-Exists/scalar/schema/in/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml#/web-api/endpoint/%2Fpsn/supportedOperation/head/returns/resp/204/header/parameter/header/X-PSN-Exists/scalar/schema/in/scalar_1
  Range: [(10,19)-(10,23)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be string
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml#/web-api/endpoint/%2Fpsn/supportedOperation/head/returns/resp/204/header/parameter/header/X-PSN-Exists/scalar/schema/in/scalar_2
  Property: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml#/web-api/endpoint/%2Fpsn/supportedOperation/head/returns/resp/204/header/parameter/header/X-PSN-Exists/scalar/schema/in/scalar_2
  Range: [(10,25)-(10,30)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml#/web-api/endpoint/%2Fpsn/supportedOperation/head/returns/resp/204/header/parameter/header/X-PSN-Exists/scalar/schema/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml#/web-api/endpoint/%2Fpsn/supportedOperation/head/returns/resp/204/header/parameter/header/X-PSN-Exists/scalar/schema/examples/example/default-example
  Range: [(11,21)-(11,27)]
  Location: file://amf-cli/shared/src/test/resources/validations/enums/enum-boolean-invalid.raml
