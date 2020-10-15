package amf.cycle

import amf.core.remote.{OasJsonHint, Vendor}

class Oas20ElementCycleTest extends DomainElementCycleTest {

  override def basePath: String = "amf-client/shared/src/test/resources/cycle/oas20/"
  val validationsPath           = "amf-client/shared/src/test/resources/validations/"
  val compatibilityPath         = "amf-client/shared/src/test/resources/compatibility/"
  val vendor: Vendor            = Vendor.OAS20

  test("type - composition with refs and inlined") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(0),
      "type/cat-emission.yaml",
      OasJsonHint
    )
  }

  test("type - schema with properties") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(1),
      "type/pet-emission.yaml",
      OasJsonHint
    )
  }

  test("type - ref to external json schema") {
    renderElement(
      "type/ref-to-external-schema.json",
      CommonExtractors.declaresIndex(0),
      "type/external-ref-emission.yaml",
      OasJsonHint
    )
  }

  test("parameter - query parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(1),
      "parameter/query-param.yaml",
      OasJsonHint
    )
  }

  test("parameter - body parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(5),
      "parameter/body-param.yaml",
      OasJsonHint
    )
  }

  test("parameter - form data parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(4),
      "parameter/form-data.yaml",
      OasJsonHint
    )
  }

  test("parameter - header parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(2),
      "parameter/header-param.yaml",
      OasJsonHint
    )
  }

  test("parameter - path parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(3),
      "parameter/path-param.yaml",
      OasJsonHint
    )
  }

  test("response - with headers and ref to schema") {
    renderElement(
      "response/response.json",
      CommonExtractors.firstResponse,
      "response/response.yaml",
      OasJsonHint
    )
  }

  test("response - external reference") {
    renderElement(
      "response/reference-external-response.json",
      CommonExtractors.firstResponse,
      "response/external-response-link-emission.yaml",
      OasJsonHint
    )
  }

  test("documentation - creative works") {
    renderElement(
      "apiWithExternalDocs.json",
      CommonExtractors.webapi.andThen(_.map(_.documentations.head)),
      "external-docs-emission.yaml",
      OasJsonHint,
      validationsPath
    )
  }

  test("security scheme") {
    renderElement(
      "api-with-security-requirement.json",
      CommonExtractors.declaresIndex(0),
      "security-scheme-emission.yaml",
      OasJsonHint,
      validationsPath + "oas-security/"
    )
  }

  test("license") {
    renderElement(
      "license/license.json",
      CommonExtractors.webapi.andThen(_.map(_.license)),
      "license/license-emission.yaml",
      OasJsonHint
    )
  }

  test("contact - organization") {
    renderElement(
      "license/license.json",
      CommonExtractors.webapi.andThen(_.map(_.provider)),
      "license/organization-emission.yaml",
      OasJsonHint
    )
  }

  test("tag") {
    renderElement(
      "oas20/custom-annotation-declaration.json",
      CommonExtractors.webapi.andThen(_.map(_.tags.head)),
      "emission/tag-emission.yaml",
      OasJsonHint,
      directory = compatibilityPath
    )
  }

  test("example") {
    renderElement(
      "response/response.json",
      CommonExtractors.firstResponse.andThen(_.map(_.examples.head)),
      "response/example.yaml",
      OasJsonHint
    )
  }

  test("endpoint") {
    renderElement(
      "response/response.json",
      CommonExtractors.firstEndpoint,
      "response/endpoint.yaml",
      OasJsonHint
    )
  }

}
