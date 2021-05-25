package amf.resolution

import amf.client.remod.amfcore.config.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Raml10, Raml10YamlHint}
import amf.emit.AMFRenderer

import scala.concurrent.{ExecutionContext, Future}

class OverlayResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/resolution/overlay/"

  test("Overlay with annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "annotations/")
  }

  test("Overlay with chained annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "annotation-chained/")
  }

  test("Overlay with documentation to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "documentation/")
  }

  test("Overlay with documentation one item to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "documentation/overlay-one-item/")
  }

  test("Overlay with examples to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "examples-mediatype/")
  }

  test("Overlay with trait annotations to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "trait-annotation/")
  }

  test("Overlay with trait displayName to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "trait-displayname/")
  }

  test("Overlay with trait empty to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "trait-empty/")
  }

  test("Overlay with types to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "types/")
  }

  test("Overlay with complex types to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "types-add/")
  }

  test("Overlay with libraries to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "with-uses/")
  }

  test("Overlay with many libraries to Raml") {
    cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "with-uses-both/")
  }

  test("Nil overlay Raml") {
    try {
      cycle("input.raml", "output.raml", Raml10YamlHint, Raml10, basePath + "nil-overlay/")
      assert(false) // should fail
    } catch {
      case e: Exception => assert(true)
    }
  }

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    val target = config.target
    new AMFRenderer(unit, target, RenderOptions().withSourceMaps.withPrettyPrint, config.syntax).renderToString
  }
}
