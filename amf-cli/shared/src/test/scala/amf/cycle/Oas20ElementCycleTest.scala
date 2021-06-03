package amf.cycle

import amf.core.remote.{Oas20JsonHint, Vendor}

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
      Oas20JsonHint
    )
  }

  test("type - schema with properties") {
    renderElement(
      "type/composition-with-refs.json",
      CommonExtractors.declaresIndex(1),
      "type/pet-emission.yaml",
      Oas20JsonHint
    )
  }

  test("type - ref to external json schema") {
    renderElement(
      "type/ref-to-external-schema.json",
      CommonExtractors.declaresIndex(0),
      "type/external-ref-emission.yaml",
      Oas20JsonHint
    )
  }

  test("parameter - query parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(1),
      "parameter/query-param.yaml",
      Oas20JsonHint
    )
  }

  test("parameter - body parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(5),
      "parameter/body-param.yaml",
      Oas20JsonHint
    )
  }

  test("parameter - form data parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(4),
      "parameter/form-data.yaml",
      Oas20JsonHint
    )
  }

  test("parameter - header parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(2),
      "parameter/header-param.yaml",
      Oas20JsonHint
    )
  }

  test("parameter - path parameter") {
    renderElement(
      "parameter/parameter-definitions.json",
      CommonExtractors.declaresIndex(3),
      "parameter/path-param.yaml",
      Oas20JsonHint
    )
  }

  test("response - with headers and ref to schema") {
    renderElement(
      "response/response.json",
      CommonExtractors.firstResponse,
      "response/response.yaml",
      Oas20JsonHint
    )
  }

  test("response - external reference") {
    renderElement(
      "response/reference-external-response.json",
      CommonExtractors.firstResponse,
      "response/external-response-link-emission.yaml",
      Oas20JsonHint
    )
  }

  test("documentation - creative works") {
    renderElement(
      "apiWithExternalDocs.json",
      CommonExtractors.webapi.andThen(_.map(_.documentations.head)),
      "external-docs-emission.yaml",
      Oas20JsonHint,
      validationsPath
    )
  }

  test("security scheme") {
    renderElement(
      "api-with-security-requirement.json",
      CommonExtractors.declaresIndex(0),
      "security-scheme-emission.yaml",
      Oas20JsonHint,
      validationsPath + "oas-security/"
    )
  }

  test("license") {
    renderElement(
      "license/license.json",
      CommonExtractors.webapi.andThen(_.map(_.license)),
      "license/license-emission.yaml",
      Oas20JsonHint
    )
  }

  test("contact - organization") {
    renderElement(
      "license/license.json",
      CommonExtractors.webapi.andThen(_.map(_.provider)),
      "license/organization-emission.yaml",
      Oas20JsonHint
    )
  }

  test("tag") {
    renderElement(
      "oas20/custom-annotation-declaration.json",
      CommonExtractors.webapi.andThen(_.map(_.tags.head)),
      "emission/tag-emission.yaml",
      Oas20JsonHint,
      directory = compatibilityPath
    )
  }

  test("example") {
    renderElement(
      "response/response.json",
      CommonExtractors.firstResponse.andThen(_.map(_.examples.head)),
      "response/example.yaml",
      Oas20JsonHint
    )
  }

  test("endpoint") {
    renderElement(
      "response/response.json",
      CommonExtractors.firstEndpoint,
      "response/endpoint.yaml",
      Oas20JsonHint
    )
  }

}
