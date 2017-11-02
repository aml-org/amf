package amf.resolution

import amf.client.GenerationOptions
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote._

import scala.concurrent.{ExecutionContext, Future}

class ExtendsRamlResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/resolution/extends/"

  test("Simple extends resolution to Raml") {
    cycle("simple-merge.raml", "simple-merge.raml.raml", RamlYamlHint, Raml)
  }

  test("Simple extends resolution to Amf") {
    cycle("simple-merge.raml", "simple-merge.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Extends resolution with parameters resolution to Raml") {
    cycle("parameters.raml", "parameters.raml.raml", RamlYamlHint, Raml)
  }

  test("Extends resolution with parameters resolution to Amf") {
    cycle("parameters.raml", "parameters.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Extends resolution with parameter and transformation resolution to Raml") {
    cycle("complex-parameters.raml", "complex-parameters.raml.raml", RamlYamlHint, Raml)
  }

  test("Extends resolution with optional method to Raml") {
    cycle("optional-method.raml", "optional-method.raml.raml", RamlYamlHint, Raml)
  }

  test("Extends resolution with optional method to Amf") {
    cycle("optional-method.raml", "optional-method.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Extends resolution with scalar collection to Raml") {
    cycle("with-collections.raml", "with-collections.raml.raml", RamlYamlHint, Raml)
  }

  test("Complex extends resolution to Raml") {
    cycle("complex-traits-resource-types.raml", "complex-traits-resource-types.raml.raml", RamlYamlHint, Raml)
  }

  test("Complex extends resolution to Amf") {
    cycle("complex-traits-resource-types.raml", "complex-traits-resource-types.raml.jsonld", RamlYamlHint, Amf)
  }

  override def render(unit: BaseUnit, config: CycleConfig): Future[String] = {
    val target = config.target
    new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
  }
}
