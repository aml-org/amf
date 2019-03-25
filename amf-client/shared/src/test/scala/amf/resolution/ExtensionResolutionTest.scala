package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml, RamlYamlHint}
import amf.emit.AMFRenderer

import scala.concurrent.{ExecutionContext, Future}

class ExtensionResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/resolution/extension/"

  test("Extension with annotations to Raml") {
    cycle("input.raml", "output.raml", RamlYamlHint, Raml, basePath + "annotations/")
  }

  test("Extension basic to Raml") {
    cycle("input.raml", "output.raml", RamlYamlHint, Raml, basePath + "basic/")
  }

  test("Extension with traits to Raml") {
    cycle("input.raml", "output.raml", RamlYamlHint, Raml, basePath + "traits/")
  }

  test("Extension with traits to Amf") {
    cycle("input.raml", "output.jsonld", RamlYamlHint, Amf, basePath + "traits/")
  }

  test("Extension chain to Raml") {
    cycle("input.raml", "output.raml", RamlYamlHint, Raml, basePath + "chain/")
  }

  test("Extension with example to Raml") {
    cycle("input.raml", "output.raml", RamlYamlHint, Raml, basePath + "example/")
  }
  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    val target = config.target
    new AMFRenderer(unit, target, RenderOptions().withSourceMaps.withPrettyPrint, config.syntax).renderToString
  }
}
