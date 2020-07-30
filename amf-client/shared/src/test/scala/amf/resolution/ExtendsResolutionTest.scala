package amf.resolution

import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.remote.{Amf, Raml, Raml08, RamlYamlHint}
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.emit.AMFRenderer
import amf.plugins.document.graph.parser.{ExpandedForm, FlattenedForm, JsonLdDocumentForm}

import scala.concurrent.{ExecutionContext, Future}

trait ExtendsResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-client/shared/src/test/resources/resolution/extends/"

  test("Simple extends resolution to Raml") {
    cycle("simple-merge.raml", "simple-merge.raml.raml", RamlYamlHint, Raml)
  }

  multiGoldenTest("Simple extends resolution to Amf", "simple-merge.raml.%s") { config =>
    cycle("simple-merge.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
  }

  test("Extends resolution with parameters resolution to Raml") {
    cycle("parameters.raml", "parameters.raml.raml", RamlYamlHint, Raml)
  }

  multiGoldenTest("Extends resolution with parameters resolution to Amf", "parameters.raml.%s") { config =>
    cycle("parameters.raml", config.golden, RamlYamlHint, target = Amf, renderOptions = Some(config.renderOptions))
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

  multiGoldenTest("Extends resolution with optional method to Amf", "optional-method.raml.%s") { config =>
    cycle("optional-method.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  test("Extends resolution with scalar collection to Raml") {
    cycle("with-collections.raml", "with-collections.raml.raml", RamlYamlHint, Raml)
  }

  test("Complex extends resolution to Raml") {
    cycle("complex-traits-resource-types.raml", "complex-traits-resource-types.raml.raml", RamlYamlHint, Raml)
  }

  multiGoldenTest("Complex extends resolution to Amf", "complex-traits-resource-types.raml.%s") { config =>
    cycle("complex-traits-resource-types.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions))
  }

  test("Traits and resourceTypes with complex variables raml to raml test") {
    cycle("resource-type-complex-variables.raml", "resource-type-complex-variables.raml.raml", RamlYamlHint, Raml)
  }

  test("Resolution using libraries to Raml") {
    cycle("traits-using-library.raml", "traits-using-library.raml.raml", RamlYamlHint, Raml)
  }

  test("Resource level trait") {
    cycle("resource_level.raml", "resource_level.raml.raml", RamlYamlHint, Raml)
  }

  test("Resolution null variable") {
    cycle("null_variable.raml", "null_variable.raml.raml", RamlYamlHint, Raml)
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

  test("Trait application with quoted value") {
    cycle("trait-with-quoted-value.raml",
          "trait-with-quoted-value.resolved.raml",
          RamlYamlHint,
          Raml08,
          basePath + "08/")
  }

  multiGoldenTest("Trait application with quoted value to jsonld", "trait-with-quoted-value.resolved.%s") { config =>
    cycle("trait-with-quoted-value.raml",
          config.golden,
          RamlYamlHint,
          target = Amf,
          directory = basePath + "08/",
          renderOptions = Some(config.renderOptions))
  }

  test("Extension with library usage") {
    cycle("extension-library/api.raml", "extension-library/api.raml.raml", RamlYamlHint, Raml)
  }

  test("Extension with library multilevel usage") {
    cycle("extension-library-multilevel/api.raml", "extension-library-multilevel/api.raml.raml", RamlYamlHint, Raml)
  }

  test("Trait with main parameter without type and trait with array type") {
    cycle("trait-parameter-infered-type-array.raml",
          "trait-parameter-infered-type-array.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("Trait with main parameter without type and trait with string type") {
    cycle("trait-parameter-infered-type-string.raml",
          "trait-parameter-infered-type-string.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("Trait with main parameter without type and trait with obj type") {
    cycle("trait-parameter-infered-type-obj.raml",
          "trait-parameter-infered-type-obj.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("Trait with main parameter without type and trait with integer type") {
    cycle("trait-parameter-infered-type-integer.raml",
          "trait-parameter-infered-type-integer.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("Trait with main parameter without type and trait with any type") {
    cycle("trait-parameter-infered-type-any.raml",
          "trait-parameter-infered-type-any.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with string type") {
    cycle("resource-type-any-infered-type-string.raml",
          "resource-type-any-infered-type-string.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with array type") {
    cycle("resource-type-any-infered-type-array.raml",
          "resource-type-any-infered-type-array.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with obj type") {
    cycle("resource-type-any-infered-type-obj.raml",
          "resource-type-any-infered-type-obj.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with any type") {
    cycle("resource-type-any-infered-type-any.raml",
          "resource-type-any-infered-type-any.raml.raml",
          RamlYamlHint,
          Raml,
          basePath)
  }

  multiGoldenTest("Test api with declared shaped reference in trait", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      RamlYamlHint,
      target = Amf,
      pipeline = Some(ResolutionPipeline.DEFAULT_PIPELINE),
      renderOptions = Some(config.renderOptions.withoutSourceMaps),
      directory = basePath + "extends-with-references-to-declares/trait/"
    )
  }

  multiGoldenTest("Test api with declared shape reference in rt", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      RamlYamlHint,
      target = Amf,
      pipeline = Some(ResolutionPipeline.DEFAULT_PIPELINE),
      renderOptions = Some(config.renderOptions.withoutSourceMaps),
      directory = basePath + "extends-with-references-to-declares/resource-type/"
    )
  }

  multiGoldenTest("Types in extends merging", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      RamlYamlHint,
      target = Amf,
      pipeline = Some(ResolutionPipeline.DEFAULT_PIPELINE),
      renderOptions = Some(config.renderOptions.withoutSourceMaps),
      directory = basePath + "extends-with-references-to-declares/merging/"
    )
  }

  test("Test complex trait with infered type") {
    cycle("trait-infered-case.raml", "trait-infered-case.raml.raml", RamlYamlHint, Raml, basePath)
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint

  override def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    val target = config.target
    new AMFRenderer(unit, target, defaultRenderOptions, config.syntax).renderToString
  }
}
