ModelId: file://amf-cli/shared/src/test/resources/validations/oas31/server-variables-enum.yaml
Profile: OAS 3.1
Conforms: false
Number of results: 1

Level: Violation

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: should be equal to one of the allowed values
  Severity: Violation
  Target: file://amf-cli/shared/src/test/resources/validations/oas31/server-variables-enum.yaml#/web-api/server/https%3A%2F%2F%7Benvironment%7D.example.com%2Fv1/variable/parameter/path/environment/scalar/environment/scalar_1
  Property: file://amf-cli/shared/src/test/resources/validations/oas31/server-variables-enum.yaml#/web-api/server/https%3A%2F%2F%7Benvironment%7D.example.com%2Fv1/variable/parameter/path/environment/scalar/environment/scalar_1
  Range: [(17,17)-(17,36)]
  Location: file://amf-cli/shared/src/test/resources/validations/oas31/server-variables-enum.yaml
