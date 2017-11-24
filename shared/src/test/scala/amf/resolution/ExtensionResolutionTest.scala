package amf.resolution

import amf.core.client.GenerationOptions
import amf.framework.model.document.BaseUnit
import amf.dumper.AMFDumper
import amf.framework.remote.{Amf, Raml, RamlYamlHint}
import amf.remote._

import scala.concurrent.{ExecutionContext, Future}

class ExtensionResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "shared/src/test/resources/resolution/extension/"

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

  override def render(unit: BaseUnit, config: CycleConfig): String = {
    val target = config.target
    new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
  }
}
