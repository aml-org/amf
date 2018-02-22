package amf.resolution

import amf.core.client.GenerationOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml, Raml08, RamlYamlHint}
import amf.facades.AMFDumper

import scala.concurrent.ExecutionContext

class ExtendsResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/resolution/extends/"

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

  test("Extends resolution with parameter and multiple transformation resolution to Raml") {
    cycle("resource-type-multi-transformation.raml",
          "resource-type-multi-transformation.raml.raml",
          RamlYamlHint,
          Raml)
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

  test("Traits and resourceTypes with complex variables raml to raml test") {
    cycle("resource-type-complex-variables.raml", "resource-type-complex-variables.raml.raml", RamlYamlHint, Raml)
  }

  test("Resolution using libraries to Raml") {
    cycle("traits-using-library.raml", "traits-using-library.raml.raml", RamlYamlHint, Raml)
  }

  test("08 Optional response resolution") {
    cycle("optional-response.raml", "optional-response.raml.raml", RamlYamlHint, Raml08, basePath + "08/")
  }

  test("08 Optional payload resolution") {
    cycle("optional-payload.raml", "optional-payload.raml.raml", RamlYamlHint, Raml08, basePath + "08/")
  }

  test("08 Optional parameters resolution") {
    cycle("optional-parameter.raml", "optional-parameter.raml.raml", RamlYamlHint, Raml08, basePath + "08/")
  }

  test("08 Optional not merge resolution") {
    cycle("optional-not-merge.raml", "optional-not-merge.raml.raml", RamlYamlHint, Raml08, basePath + "08/")
  }

  test("08 Usage in traits.") {
    cycle("usage.raml", "usage.raml.raml", RamlYamlHint, Raml08, basePath + "08/")
  }

  override def render(unit: BaseUnit, config: CycleConfig): String = {
    val target = config.target
    new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
  }
}
