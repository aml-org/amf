package amf.cycle

import amf.core.remote.{RamlYamlHint, Vendor}

class Raml08ElementCycleTest extends DomainElementCycleTest {

  override def basePath: String = "amf-client/shared/src/test/resources/upanddown/"
  val cyclePath: String         = "amf-client/shared/src/test/resources/cycle/raml08/"
  val vendor: Vendor            = Vendor.RAML08

  test("type - inlined json schema") {
    renderElement(
      "schema-position/api.raml",
      CommonExtractors.declaresIndex(0),
      "schema-position/type-emission.yaml",
      RamlYamlHint,
      directory = basePath + "cycle/raml08/"
    )
  }

  test("type - reference to external json schema with relative path") {
    renderElement(
      "raml08/json_schema_array.raml",
      CommonExtractors.declaresIndex(0),
      "raml08/json_schema_array-type.yaml",
      RamlYamlHint
    )
  }

  test("trait") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(1),
      "abstract/trait-emission.yaml",
      RamlYamlHint,
      directory = cyclePath
    )
  }

  test("resource type") {
    renderElement(
      "abstract/rt-and-trait-definition.raml",
      CommonExtractors.declaresIndex(0),
      "abstract/rt-emission.yaml",
      RamlYamlHint,
      directory = cyclePath
    )
  }
}
