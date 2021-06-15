package amf.resolution

import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.transform.pipelines.TransformationPipeline
import amf.core.internal.remote._

import scala.concurrent.ExecutionContext

trait ExtendsResolutionTest extends ResolutionTest {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "amf-cli/shared/src/test/resources/resolution/extends/"

  test("Simple extends resolution to Raml") {
    cycle("simple-merge.raml", "simple-merge.raml.raml", Raml10YamlHint, Raml10)
  }

  multiGoldenTest("Simple extends resolution to Amf", "simple-merge.raml.%s") { config =>
    cycle("simple-merge.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  test("Extends resolution with parameters resolution to Raml") {
    cycle("parameters.raml", "parameters.raml.raml", Raml10YamlHint, Raml10)
  }

  multiGoldenTest("Extends resolution with parameters resolution to Amf", "parameters.raml.%s") { config =>
    cycle("parameters.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  test("Extends resolution with parameter and transformation resolution to Raml") {
    cycle("complex-parameters.raml", "complex-parameters.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Extends resolution with parameter and multiple transformation resolution to Raml") {
    cycle("resource-type-multi-transformation.raml",
          "resource-type-multi-transformation.raml.raml",
          Raml10YamlHint,
          Raml10)
  }

  test("Extends resolution with optional method to Raml") {
    cycle("optional-method.raml", "optional-method.raml.raml", Raml10YamlHint, Raml10)
  }

  multiGoldenTest("Extends resolution with optional method to Amf", "optional-method.raml.%s") { config =>
    cycle("optional-method.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  test("Extends resolution with scalar collection to Raml") {
    cycle("with-collections.raml", "with-collections.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Complex extends resolution to Raml") {
    cycle("complex-traits-resource-types.raml", "complex-traits-resource-types.raml.raml", Raml10YamlHint, Raml10)
  }

  multiGoldenTest("Complex extends resolution to Amf", "complex-traits-resource-types.raml.%s") { config =>
    cycle("complex-traits-resource-types.raml",
          config.golden,
          Raml10YamlHint,
          target = Amf,
          renderOptions = Some(config.renderOptions),
          transformWith = Some(Raml10))
  }

  test("Traits and resourceTypes with complex variables raml to raml test") {
    cycle("resource-type-complex-variables.raml", "resource-type-complex-variables.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Resolution using libraries to Raml") {
    cycle("traits-using-library.raml", "traits-using-library.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Resource level trait") {
    cycle("resource_level.raml", "resource_level.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Resolution null variable") {
    cycle("null_variable.raml", "null_variable.raml.raml", Raml10YamlHint, Raml10)
  }

  test("08 Optional response resolution") {
    cycle("optional-response.raml", "optional-response.raml.raml", Raml08YamlHint, Raml08, basePath + "08/")
  }

  test("08 Optional payload resolution") {
    cycle("optional-payload.raml", "optional-payload.raml.raml", Raml08YamlHint, Raml08, basePath + "08/")
  }

  test("08 Optional parameters resolution") {
    cycle("optional-parameter.raml", "optional-parameter.raml.raml", Raml08YamlHint, Raml08, basePath + "08/")
  }

  test("08 Optional not merge resolution") {
    cycle("optional-not-merge.raml", "optional-not-merge.raml.raml", Raml08YamlHint, Raml08, basePath + "08/")
  }

  test("08 Usage in traits.") {
    cycle("usage.raml", "usage.raml.raml", Raml08YamlHint, Raml08, basePath + "08/")
  }

  test("Trait application with quoted value") {
    cycle("trait-with-quoted-value.raml",
          "trait-with-quoted-value.resolved.raml",
          Raml08YamlHint,
          Raml08,
          basePath + "08/",
          transformWith = Some(Raml10))
  }

  multiGoldenTest("Trait application with quoted value to jsonld", "trait-with-quoted-value.resolved.%s") { config =>
    cycle(
      "trait-with-quoted-value.raml",
      config.golden,
      Raml08YamlHint,
      target = Amf,
      directory = basePath + "08/",
      renderOptions = Some(config.renderOptions),
      transformWith = Some(Raml10)
    )
  }

  test("Extension with library usage") {
    cycle("extension-library/api.raml", "extension-library/api.raml.raml", Raml10YamlHint, Raml10)
  }

  test("Extension with library multilevel usage") {
    cycle("extension-library-multilevel/api.raml",
          "extension-library-multilevel/api.raml.raml",
          Raml10YamlHint,
          Raml10)
  }

  test("Trait with main parameter without type and trait with array type") {
    cycle("trait-parameter-infered-type-array.raml",
          "trait-parameter-infered-type-array.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("Trait with main parameter without type and trait with string type") {
    cycle("trait-parameter-infered-type-string.raml",
          "trait-parameter-infered-type-string.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("Trait with main parameter without type and trait with obj type") {
    cycle("trait-parameter-infered-type-obj.raml",
          "trait-parameter-infered-type-obj.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("Trait with main parameter without type and trait with integer type") {
    cycle("trait-parameter-infered-type-integer.raml",
          "trait-parameter-infered-type-integer.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("Trait with main parameter without type and trait with any type") {
    cycle("trait-parameter-infered-type-any.raml",
          "trait-parameter-infered-type-any.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with string type") {
    cycle("resource-type-any-infered-type-string.raml",
          "resource-type-any-infered-type-string.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with array type") {
    cycle("resource-type-any-infered-type-array.raml",
          "resource-type-any-infered-type-array.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with obj type") {
    cycle("resource-type-any-infered-type-obj.raml",
          "resource-type-any-infered-type-obj.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  test("ResourceType with main shape without type and resourceType with any type") {
    cycle("resource-type-any-infered-type-any.raml",
          "resource-type-any-infered-type-any.raml.raml",
          Raml10YamlHint,
          Raml10,
          basePath)
  }

  multiGoldenTest("Test api with declared shaped reference in trait", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      pipeline = Some(TransformationPipeline.DEFAULT_PIPELINE),
      renderOptions = Some(config.renderOptions.withoutSourceMaps),
      directory = basePath + "extends-with-references-to-declares/trait/",
      transformWith = Some(Raml10)
    )
  }

  multiGoldenTest("Test api with declared shape reference in rt", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      pipeline = Some(TransformationPipeline.DEFAULT_PIPELINE),
      renderOptions = Some(config.renderOptions.withoutSourceMaps),
      directory = basePath + "extends-with-references-to-declares/resource-type/",
      transformWith = Some(Raml10)
    )
  }

  multiGoldenTest("Types in extends merging", "api.%s") { config =>
    cycle(
      "api.raml",
      config.golden,
      Raml10YamlHint,
      target = Amf,
      pipeline = Some(TransformationPipeline.DEFAULT_PIPELINE),
      renderOptions = Some(config.renderOptions.withoutSourceMaps),
      directory = basePath + "extends-with-references-to-declares/merging/",
      transformWith = Some(Raml10)
    )
  }

  test("Test complex trait with infered type") {
    cycle("trait-infered-case.raml", "trait-infered-case.raml.raml", Raml10YamlHint, Raml10, basePath)
  }

  override def defaultRenderOptions: RenderOptions = RenderOptions().withSourceMaps.withPrettyPrint
}
