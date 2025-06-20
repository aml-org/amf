package amf.validation

import amf.apicontract.client.scala.OASConfiguration

class OasJsonModelUniquePlatformReportTest extends UniquePlatformReportGenTest {

  override val basePath    = "file://amf-cli/shared/src/test/resources/validations/"
  override val reportsPath = "amf-cli/shared/src/test/resources/validations/reports/model/"
  val cyclePath: String    = "file://amf-cli/shared/src/test/resources/upanddown/oas31/"

  test("Tags in oas") {
    validate("/webapi/tags.json", Some("webapi-tags.report"))
  }

  test("Parse and validate invalid responses") {
    validate("invalid-status-code-string/api.json", Some("invalid-status-code-string-oas.report"))
  }

  test("Parameter without shape") {
    validate("parameter-without-shape/parameter-without-shape.json", Some("parameter-without-shape.report"))
  }

  test("Invalid required in oas schema") {
    validate("invalid-oas-required/invalid-oas-required.json", Some("invalid-oas-required.report"))
  }

  test("Warning when using raml security schemes") {
    validate("raml-security-in-oas.json", Some("raml-security-in-oas.report"))
  }

  test("Path parameter must have the property required defined") {
    validate("path-parameter-required/required-is-not-present.json", Some("required-is-not-present.report"))
  }

  test("Path parameters must have required set to true") {
    validate("path-parameter-required/required-set-to-false.json", Some("required-set-to-false.report"))
  }

  test("Operation ids are unique") {
    validate("duplicate-operation-ids.json", Some("duplicate-operation-ids.report"))
  }

  test("Duplicate query parameters both defined inline") {
    validate("duplicate-parameters/duplicate-query-parameters.json", Some("duplicate-query-parameters.report"))
  }

  test("Duplicate formData parameters both defined inline") {
    validate("duplicate-parameters/duplicate-form-parameters.json", Some("duplicate-form-parameters.report"))
  }

  test("Read only property marked as required") {
    validate("read-only-property-marked-required.json", Some("read-only-property-marked-required.report"))
  }

  test("Duplicate formData parameters defined with reference") {
    validate("duplicate-parameters/duplicate-parameters-with-ref.json", Some("duplicate-parameters-with-ref.report"))
  }

  test("Three duplicated query parameters with reference and inline") {
    validate(
      "duplicate-parameters/three-duplicated-query-parameters.json",
      Some("three-duplicated-query-parameters.report")
    )
  }

  test("Parameters of type file defined in path with invalid consumes property") {
    validate("file-parameter-consumes/parameter-in-path.json", Some("path-file-parameter-invalid-consumes.report"))
  }

  test("Parameters of type file defined in operation with invalid consumes property") {
    validate(
      "file-parameter-consumes/parameter-in-operation.json",
      Some("operation-file-parameter-invalid-consumes.report")
    )
  }

  test("Parameter of type file with no consumes property defined") {
    validate("file-parameter-consumes/no-consumes-defined.json", Some("file-parameter-no-consumes.report"))
  }

  test("file parameter with incorrect binding") {
    validate("file-parameter/file-parameter-incorrect-binding.json", Some("file-parameter-incorrect-binding.report"))
  }

  test("Examples with invalid mime type") {
    validate("examples-mime-type/invalid-mime-type.json", Some("invalid-mime-type.report"))
  }

  test("Response with examples defined and no schema") {
    validate("examples-mime-type/response-with-no-schema.json", Some("response-with-no-schema.report"))
  }

  test("Invalid paths in paths object") {
    validate("invalid-oas-path/invalid-oas-path.json", Some("invalid-oas-path.report"))
  }

  test("Response status code wildcards") {
    validate(
      "../upanddown/oas3/response-code-wildcards.json",
      Some("response-code-wildcards.report")
    )
  }

  test("Response object with no description") {
    validate(
      "../upanddown/oas3/response-no-description.json",
      Some("response-missing-description.report")
    )
  }

  test("Unique name for tags") {
    validate(
      "../upanddown/oas3/unique-name-for-tags.json",
      Some("unique-name-for-tags.report")
    )
  }

  test("Valid format of email address") {
    validate(
      "../upanddown/oas3/invalid-email-address.json",
      Some("invalid-email-address.report")
    )
  }

  test("Request body must define content field, discriminator must define propertyName") {
    validate(
      "../upanddown/oas3/request-body-and-discriminator-required-fields.json",
      Some("request-body-and-discriminator-required-fields.report")
    )
  }

  test("Mutually exclusive fields in example") {
    validate(
      "../upanddown/oas3/basic-content.json",
      Some("example-mutually-exclusive-fields.report")
    )
  }

  test("Components must use keys with certain regex") {
    validate(
      "../upanddown/oas3/components/invalid-component-keys.json",
      Some("components-regex.report")
    )
  }

  test("Parameter must define a schema or content property") {
    validate(
      "../upanddown/oas3/basic-parameters/parameter-schema-and-content.json",
      Some("param-schema-or-content.report")
    )
  }

  test("Parameter content must contain only one entry") {
    validate(
      "../upanddown/oas3/basic-parameters/parameter-multiple-content-entries.json",
      Some("parameter-multiple-content-entries.report")
    )
  }

  test("OAS 3 schema object validations") {
    validate(
      "../upanddown/oas3/schema-definitions.json",
      Some("oas3-schema-validations.report")
    )
  }

  test("Templated paths with same hierarchy must not exist") {
    validate(
      "oas3/paths-with-same-hierarchy.json",
      Some("paths-with-same-hierarchy.report")
    )
  }

  test("Fields with mandatory valid URLs") {
    validate("oas3/invalid-urls.json", Some("invalid-urls.report"))
  }

  test("Server variables with missing default field") {
    validate(
      "oas3/server-variable-missing-field.json",
      Some("server-variable-missing-default-field.report")
    )
  }

  test("Security requirement object with non empty scopes array") {
    validate(
      "oas3/security-definition-non-empty-scopes.json",
      Some("security-requirement-non-empty-scopes.report")
    )
  }

  test("Security schemes validations") {
    validate(
      "oas3/invalid-security-schemes.json",
      Some("invalid-security-schemes.report")
    )
  }

  test("Runtime expression validations") {
    validate(
      "oas3/runtime-expressions.json",
      Some("runtime-expressions.report")
    )
  }

  test("Unresolved refs defined in components") {
    validate(
      "oas3/unresolved-refs-in-components.json",
      Some("unresolved-refs-in-components.report")
    )
  }

  test("Unresolved ref in schema") {
    validate(
      "oas3/unresolved-ref-in-schema.json",
      Some("unresolved-ref-in-schema.report")
    )
  }

  test("Closed shape schema on response node") {
    validate(
      "oas3/schema-on-response-node.json",
      Some("schema-on-response-node.report")
    )
  }

  test("Invalid type in operation tags") {
    validate(
      "oas3/invalid-tags-type.json",
      Some("invalid-tags-type.report")
    )
  }

  test("Multiple link references") {
    validate("oas3/multiple-links2.json")
  }

  test("Multiple link references 2") {
    validate("oas3/multiple-links3.json")

  }

  test("Closed shape in components, servers, and example") {
    validate(
      "oas3/oas3-closed-shapes.json",
      Some("oas3-closed-shapes.report")
    )
  }

  test("OAS 3.1 fields not supported in OAS 3.0") {
    validate(
      "oas3/oas31-fields-not-supported-in-oas30.json",
      Some("oas31-fields-not-supported-in-oas30.report")
    )
  }

  test("Json Ref - Invalid path in valid external fragment") {
    validate("oas2/invalid-ref-valid-fragment/api.json", Some("invalid-ref-valid-fragment.report"))
  }

  test("OAS 3.1 full API should be valid") {
    validate(
      "oas-31-full.json",
      None,
      configOverride = Some(OASConfiguration.OAS31()),
      directory = cyclePath,
      ignoreWarnings = true // It is a warning in a field which validation use is still not implemented)
    )
  }
}
