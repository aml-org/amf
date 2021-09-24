ModelId: file://amf-cli/shared/src/test/resources/validations/examples/examples-in-oas.json
Profile: OAS 2.0
Conforms: true
Number of results: 2

Level: Warning

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: dateOfBirth should match format "date-time"
phoneNo should be integer

  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples-in-oas.json#/declares/shape/User/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples-in-oas.json#/declares/shape/User/examples/example/default-example
  Range: [(257,18)-(284,7)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples-in-oas.json

- Constraint: http://a.ml/vocabularies/amf/validation#example-validation-error
  Message: application.user.dateOfBirth should match format "date-time"
application.user.monthsAtCurrAdd should be integer
user.dateOfBirth should match format "date-time"
user.monthsAtCurrAdd should be integer

  Severity: Warning
  Target: file://amf-cli/shared/src/test/resources/validations/examples/examples-in-oas.json#/declares/shape/LoanApplication/examples/example/default-example
  Property: file://amf-cli/shared/src/test/resources/validations/examples/examples-in-oas.json#/declares/shape/LoanApplication/examples/example/default-example
  Range: [(340,18)-(438,7)]
  Location: file://amf-cli/shared/src/test/resources/validations/examples/examples-in-oas.json
