package amf.validation

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, Oas30JsonHint, Oas30YamlHint}
import amf.io.FunSuiteCycleTests

class OASExtensionsTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/validations/oas3/spec-extensions/"
  override def renderOptions(): RenderOptions = RenderOptions().withPrettyPrint.withSourceMaps.withRawSourceMaps

  test("Extension in info Oas to Amf") {
    cycle(
      "extension-in-info.yaml",
      "extension-in-info.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("Extension in server variable Oas to Amf") {
    cycle(
      "extension-in-server.yaml",
      "extension-in-server.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("Extension in xml object Oas to Amf") {
    cycle(
      "extension-in-xml-object.yaml",
      "extension-in-xml-object.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("Extension in flows Oas to Amf") {
    cycle(
      "extension-in-flows.yaml",
      "extension-in-flows.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("Extension in flow Oas to Amf") {
    cycle(
      "extension-in-flow.yaml",
      "extension-in-flow.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("Extension in components Oas to Amf") {
    cycle(
      "extension-in-components.yaml",
      "extension-in-components.jsonld",
      Oas30JsonHint,
      target = AmfJsonHint,
    )
  }

  test("Extension in info Oas to Oas") {
    cycle(
      "extension-in-info.yaml",
      "extension-in-info-resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("Extension in server Oas to Oas") {
    cycle(
      "extension-in-server.yaml",
      "extension-in-server-resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("Extension in xml object Oas to Oas") {
    cycle(
      "extension-in-xml-object.yaml",
      "extension-in-xml-object-resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("Extension in flows Oas to Oas") {
    cycle(
      "extension-in-flows.yaml",
      "extension-in-flows-resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("Extension in flow Oas to Oas") {
    cycle(
      "extension-in-flow.yaml",
      "extension-in-flow-resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("Extension in components Oas to Oas") {
    cycle(
      "extension-in-components.yaml",
      "extension-in-components-resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }
}
