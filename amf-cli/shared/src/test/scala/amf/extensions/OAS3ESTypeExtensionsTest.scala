package amf.extensions

import amf.core.client.scala.config.RenderOptions
import amf.core.internal.remote.{AmfJsonHint, Oas30YamlHint}
import amf.io.FunSuiteCycleTests

class OAS3ESTypeExtensionsTest extends FunSuiteCycleTests {
  override def basePath: String = "amf-cli/shared/src/test/resources/validations/oas3/es-type-extensions/"

  override def renderOptions(): RenderOptions = RenderOptions().withPrettyPrint.withSourceMaps.withRawSourceMaps

  test("ES Extension in root to JSON-LD") {
    cycle(
      "es-extension-root.yaml",
      "es-extension-root.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("ES Extension in root cycle") {
    cycle(
      "es-extension-root.yaml",
      "es-extension-root.resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("ES Extension in operation to JSON-LD") {
    cycle(
      "es-extension-operation.yaml",
      "es-extension-operation.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("ES Extension in operation cycle") {
    cycle(
      "es-extension-operation.yaml",
      "es-extension-operation.resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("ES Extension in parameter to JSON-LD") {
    cycle(
      "es-extension-parameter.yaml",
      "es-extension-parameter.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("ES Extension in parameter cycle") {
    cycle(
      "es-extension-parameter.yaml",
      "es-extension-parameter.resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("ES Extension in Schema to JSON-LD") {
    cycle(
      "es-extension-shape.yaml",
      "es-extension-shape.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("ES Extension in Schema cycle") {
    cycle(
      "es-extension-shape.yaml",
      "es-extension-shape.resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }

  test("ES Extension full example to JSON-LD") {
    cycle(
      "es-extension-full.yaml",
      "es-extension-full.jsonld",
      Oas30YamlHint,
      target = AmfJsonHint,
    )
  }

  test("ES Extension full example cycle") {
    cycle(
      "es-extension-full.yaml",
      "es-extension-full.resolved.yaml",
      Oas30YamlHint,
      target = Oas30YamlHint,
    )
  }
}
