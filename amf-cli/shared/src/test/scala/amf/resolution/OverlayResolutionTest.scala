package amf.resolution

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.remote.{Raml10, Raml10YamlHint}
import amf.testing.Raml10Yaml

import scala.concurrent.ExecutionContext

class OverlayResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-cli/shared/src/test/resources/resolution/overlay/"

  test("Overlay with annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "annotations/")
  }

  test("Overlay with chained annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "annotation-chained/")
  }

  test("Overlay with documentation to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "documentation/")
  }

  test("Overlay with documentation one item to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "documentation/overlay-one-item/")
  }

  test("Overlay with examples to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "examples-mediatype/")
  }

  test("Overlay with trait annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "trait-annotation/")
  }

  test("Overlay with trait displayName to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "trait-displayname/")
  }

  test("Overlay with trait empty to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "trait-empty/")
  }

  test("Overlay with types to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "types/")
  }

  test("Overlay with complex types to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "types-add/")
  }

  test("Overlay with libraries to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "with-uses/")
  }

  test("Overlay with many libraries to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "with-uses-both/")
  }

  test("Nil overlay Raml") {
    try {
      cycle("input.raml", "output.raml", Raml10YamlHint, Raml10Yaml, basePath + "nil-overlay/")
      assert(false) // should fail
    } catch {
      case e: Exception => assert(true)
    }
  }

  override def render(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): String = {
    super.render(unit,
                 config,
                 amfConfig.withRenderOptions(amfConfig.options.renderOptions.withSourceMaps.withPrettyPrint))
  }
}
