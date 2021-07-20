package amf.emit

import amf.apicontract.client.scala.AMFConfiguration
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.common.transform._
import amf.core.internal.remote._
import amf.io.FunSuiteCycleTests

// This test suite test the generation of RAML unions
class UnionRamlEmissionTest extends FunSuiteCycleTests {

  override val basePath = "amf-cli/shared/src/test/resources/union/"

  override def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = {
    val res: BaseUnit = config.pipeline match {
      case Some(pipeline) =>
        amfConfig
          .withErrorHandlerProvider(() => UnhandledErrorHandler)
          .baseUnitClient()
          .transform(unit, pipeline)
          .baseUnit
      case None => unit
    }
    res
  }

  test("Basic scalar - No Resolution") {
    cycle("basic-scalar.raml", "basic-scalar.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Basic scalar - Editing Resolution") {
    cycle("basic-scalar.raml",
          "basic-scalar.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Basic scalar - Default Resolution") {
    cycle("basic-scalar.raml",
          "basic-scalar.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Complex scalar - No Resolution") {
    cycle("complex-scalar.raml", "complex-scalar.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Complex scalar - Editing Resolution") {
    cycle("complex-scalar.raml",
          "complex-scalar.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Complex scalar - Default Resolution") {
    cycle("complex-scalar.raml",
          "complex-scalar.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Basic Types - No Resolution") {
    cycle("basic-types.raml", "basic-types.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Basic Types - Editing Resolution") {
    cycle("basic-types.raml",
          "basic-types.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Basic Types - Default Resolution") {
    cycle("basic-types.raml",
          "basic-types.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Object - No Resolution") {
    cycle("object.raml", "object.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Object - Editing Resolution") {
    cycle("object.raml", "object.out.editing.raml", Raml10YamlHint, Raml10, pipeline = Some(PipelineId.Editing))
  }

  test("Object - Default Resolution") {
    cycle("object.raml", "object.out.default.raml", Raml10YamlHint, Raml10, pipeline = Some(PipelineId.Default))
  }

  test("Objects - No Resolution") {
    cycle("objects.raml", "objects.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Objects - Editing Resolution") {
    cycle("objects.raml", "objects.out.editing.raml", Raml10YamlHint, Raml10, pipeline = Some(PipelineId.Editing))
  }

  test("Objects - Default Resolution") {
    cycle("objects.raml", "objects.out.default.raml", Raml10YamlHint, Raml10, pipeline = Some(PipelineId.Default))
  }

  test("Complex object - No Resolution") {
    cycle("complex-object.raml", "complex-object.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Complex object - Editing Resolution") {
    cycle("complex-object.raml",
          "complex-object.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Complex object - Default Resolution") {
    cycle("complex-object.raml",
          "complex-object.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Expanded anyOf - Simple Scalar - No Resolution") {
    cycle("anyof-simple-scalar.raml", "anyof-simple-scalar.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Expanded anyOf - Simple Scalar - Editing Resolution") {
    cycle("anyof-simple-scalar.raml",
          "anyof-simple-scalar.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Expanded anyOf - Simple Scalar - Default Resolution") {
    cycle("anyof-simple-scalar.raml",
          "anyof-simple-scalar.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Expanded anyOf - Complex Scalar - No Resolution") {
    cycle("anyof-complex-scalar.raml", "anyof-complex-scalar.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Expanded anyOf - Complex Scalar - Editing Resolution") {
    cycle("anyof-complex-scalar.raml",
          "anyof-complex-scalar.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Expanded anyOf - Complex Scalar - Default Resolution") {
    cycle("anyof-complex-scalar.raml",
          "anyof-complex-scalar.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Optional Scalar - No Resolution") {
    cycle("optional-scalar.raml", "optional-scalar.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Optional Scalar - Editing Resolution") {
    cycle("optional-scalar.raml",
          "optional-scalar.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Optional Scalar - Default Resolution") {
    cycle("optional-scalar.raml",
          "optional-scalar.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Optional Object - No Resolution") {
    cycle("optional-object.raml", "optional-object.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Optional Object - Editing Resolution") {
    cycle("optional-object.raml",
          "optional-object.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Optional Object - Default Resolution") {
    cycle("optional-object.raml",
          "optional-object.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Recursive - No Resolution") {
    cycle("recursive.raml", "recursive.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  // TODO testd because we are not emitting the recursives shapes (!!!)
  ignore("Recursive - Editing Resolution") {
    cycle("recursive.raml", "recursive.out.editing.raml", Raml10YamlHint, Raml10, pipeline = Some(PipelineId.Editing))
  }

  ignore("Recursive - Default Resolution") {
    cycle("recursive.raml", "recursive.out.default.raml", Raml10YamlHint, Raml10, pipeline = Some(PipelineId.Default))
  }

  test("Inner Union - No Resolution") {
    cycle("inner-union.raml", "inner-union.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Inner Union - Editing Resolution") {
    cycle("inner-union.raml",
          "inner-union.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Inner Union - Default Resolution") {
    cycle("inner-union.raml",
          "inner-union.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

  test("Complex Union - No Resolution") {
    cycle("complex-union.raml", "complex-union.out.raml", Raml10YamlHint, Raml10, pipeline = None)
  }

  test("Complex Union - Editing Resolution") {
    cycle("complex-union.raml",
          "complex-union.out.editing.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Editing))
  }

  test("Complex Union - Default Resolution") {
    cycle("complex-union.raml",
          "complex-union.out.default.raml",
          Raml10YamlHint,
          Raml10,
          pipeline = Some(PipelineId.Default))
  }

}
