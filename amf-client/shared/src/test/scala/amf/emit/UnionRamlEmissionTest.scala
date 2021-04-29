package amf.emit

import amf.core.errorhandling.UnhandledErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.remote._
import amf.core.resolution.pipelines.ResolutionPipeline
import amf.core.resolution.pipelines.ResolutionPipeline._
import amf.core.services.RuntimeResolver
import amf.io.FunSuiteCycleTests
import amf.plugins.document.webapi.Raml10Plugin

// This test suite test the generation of RAML unions
class UnionRamlEmissionTest extends FunSuiteCycleTests {

  override val basePath = "amf-client/shared/src/test/resources/union/"

  override def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val res = config.pipeline match {
      case Some(pipeline) => RuntimeResolver.resolve(Vendor.RAML10.name, unit, pipeline, UnhandledErrorHandler)
      case None           => unit
    }
    res
  }

  test("Basic scalar - No Resolution") {
    cycle("basic-scalar.raml", "basic-scalar.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Basic scalar - Editing Resolution") {
    cycle("basic-scalar.raml",
          "basic-scalar.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Basic scalar - Default Resolution") {
    cycle("basic-scalar.raml",
          "basic-scalar.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Complex scalar - No Resolution") {
    cycle("complex-scalar.raml", "complex-scalar.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Complex scalar - Editing Resolution") {
    cycle("complex-scalar.raml",
          "complex-scalar.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Complex scalar - Default Resolution") {
    cycle("complex-scalar.raml",
          "complex-scalar.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Basic Types - No Resolution") {
    cycle("basic-types.raml", "basic-types.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Basic Types - Editing Resolution") {
    cycle("basic-types.raml", "basic-types.out.editing.raml", RamlYamlHint, Raml10, pipeline = Some(EDITING_PIPELINE))
  }

  test("Basic Types - Default Resolution") {
    cycle("basic-types.raml", "basic-types.out.default.raml", RamlYamlHint, Raml10, pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Object - No Resolution") {
    cycle("object.raml", "object.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Object - Editing Resolution") {
    cycle("object.raml", "object.out.editing.raml", RamlYamlHint, Raml10, pipeline = Some(EDITING_PIPELINE))
  }

  test("Object - Default Resolution") {
    cycle("object.raml", "object.out.default.raml", RamlYamlHint, Raml10, pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Objects - No Resolution") {
    cycle("objects.raml", "objects.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Objects - Editing Resolution") {
    cycle("objects.raml", "objects.out.editing.raml", RamlYamlHint, Raml10, pipeline = Some(EDITING_PIPELINE))
  }

  test("Objects - Default Resolution") {
    cycle("objects.raml", "objects.out.default.raml", RamlYamlHint, Raml10, pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Complex object - No Resolution") {
    cycle("complex-object.raml", "complex-object.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Complex object - Editing Resolution") {
    cycle("complex-object.raml",
          "complex-object.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Complex object - Default Resolution") {
    cycle("complex-object.raml",
          "complex-object.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Expanded anyOf - Simple Scalar - No Resolution") {
    cycle("anyof-simple-scalar.raml", "anyof-simple-scalar.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Expanded anyOf - Simple Scalar - Editing Resolution") {
    cycle("anyof-simple-scalar.raml",
          "anyof-simple-scalar.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Expanded anyOf - Simple Scalar - Default Resolution") {
    cycle("anyof-simple-scalar.raml",
          "anyof-simple-scalar.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Expanded anyOf - Complex Scalar - No Resolution") {
    cycle("anyof-complex-scalar.raml", "anyof-complex-scalar.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Expanded anyOf - Complex Scalar - Editing Resolution") {
    cycle("anyof-complex-scalar.raml",
          "anyof-complex-scalar.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Expanded anyOf - Complex Scalar - Default Resolution") {
    cycle("anyof-complex-scalar.raml",
          "anyof-complex-scalar.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Optional Scalar - No Resolution") {
    cycle("optional-scalar.raml", "optional-scalar.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Optional Scalar - Editing Resolution") {
    cycle("optional-scalar.raml",
          "optional-scalar.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Optional Scalar - Default Resolution") {
    cycle("optional-scalar.raml",
          "optional-scalar.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Optional Object - No Resolution") {
    cycle("optional-object.raml", "optional-object.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Optional Object - Editing Resolution") {
    cycle("optional-object.raml",
          "optional-object.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Optional Object - Default Resolution") {
    cycle("optional-object.raml",
          "optional-object.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Recursive - No Resolution") {
    cycle("recursive.raml", "recursive.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  // TODO testd because we are not emitting the recursives shapes (!!!)
  ignore("Recursive - Editing Resolution") {
    cycle("recursive.raml", "recursive.out.editing.raml", RamlYamlHint, Raml10, pipeline = Some(EDITING_PIPELINE))
  }

  ignore("Recursive - Default Resolution") {
    cycle("recursive.raml", "recursive.out.default.raml", RamlYamlHint, Raml10, pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Inner Union - No Resolution") {
    cycle("inner-union.raml", "inner-union.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Inner Union - Editing Resolution") {
    cycle("inner-union.raml", "inner-union.out.editing.raml", RamlYamlHint, Raml10, pipeline = Some(EDITING_PIPELINE))
  }

  test("Inner Union - Default Resolution") {
    cycle("inner-union.raml", "inner-union.out.default.raml", RamlYamlHint, Raml10, pipeline = Some(DEFAULT_PIPELINE))
  }

  test("Complex Union - No Resolution") {
    cycle("complex-union.raml", "complex-union.out.raml", RamlYamlHint, Raml10, pipeline = None)
  }

  test("Complex Union - Editing Resolution") {
    cycle("complex-union.raml",
          "complex-union.out.editing.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(EDITING_PIPELINE))
  }

  test("Complex Union - Default Resolution") {
    cycle("complex-union.raml",
          "complex-union.out.default.raml",
          RamlYamlHint,
          Raml10,
          pipeline = Some(DEFAULT_PIPELINE))
  }

}
