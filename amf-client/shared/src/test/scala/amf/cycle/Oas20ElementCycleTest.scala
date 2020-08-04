package amf.cycle

import amf.core.remote.{OasJsonHint, Vendor}

class Oas20ElementCycleTest extends DomainElementCycleTest {

  override def basePath: String = "amf-client/shared/src/test/resources/cycle/oas20/"
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
}
