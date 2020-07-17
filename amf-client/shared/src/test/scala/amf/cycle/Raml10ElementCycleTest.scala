package amf.cycle

import amf.core.remote.{RamlYamlHint, Vendor}

class Raml10ElementCycleTest extends DomainElementCycleTest {

  val basePath: String       = "amf-client/shared/src/test/resources/cycle/raml10/"
  val jsonSchemaPath: String = "amf-client/shared/src/test/resources/org/raml/json_schema/"
  val vendor: Vendor         = Vendor.RAML10

  test("type - multiple inheritance with union and properties") {
    renderElement(
      "type/complex-inheritance-unions.raml",
      CommonExtractors.namedRootInDeclares,
      "type/complex-inheritance-unions.yaml",
      RamlYamlHint
    )
  }

  test("type - reference to external fragment") {
    renderElement(
      "multiple-refs/input.raml",
      CommonExtractors.namedRootInDeclares,
      "multiple-refs/type-cycle-emission.yaml",
      RamlYamlHint,
      directory = jsonSchemaPath
    )
  }

  test("response - named example included") {
    renderElement(
      "response/input.raml",
      CommonExtractors.firstResponse,
      "response/output.yaml",
      RamlYamlHint
    )
  }

}
