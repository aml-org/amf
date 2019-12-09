package amf.io

import amf.core.client.ParsingOptions
import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.rdf.RdfModel
import amf.core.remote.Syntax.Syntax
import amf.core.remote.{Amf, Hint, Vendor}
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Cycle tests using temporary file and directory creator
  */
abstract class FunSuiteCycleTests extends AsyncFunSuite with BuildCycleTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}

trait BuildCycleTests extends FileAssertionTest {

  private implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  def basePath: String

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, syntax: Option[Syntax]): Future[Assertion] =
    cycle(source, hint, basePath, syntax)

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint): Future[Assertion] = cycle(source, hint, basePath, None)

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, directory: String, syntax: Option[Syntax]): Future[Assertion] =
    cycle(source, source, hint, hint.vendor, directory, None, syntax = syntax)

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, directory: String): Future[Assertion] =
    cycle(source, source, hint, hint.vendor, directory, None)

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  final def cycle(source: String,
                  golden: String,
                  hint: Hint,
                  target: Vendor,
                  directory: String = basePath,
                  validation: Option[Validation] = None,
                  useAmfJsonldSerialization: Boolean = true,
                  syntax: Option[Syntax] = None,
                  pipeline: Option[String] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory, syntax, pipeline)

    build(config, validation, useAmfJsonldSerialization)
      .map(transform(_, config))
      .flatMap(render(_, config, useAmfJsonldSerialization))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  /** Method to parse unit. Override if necessary. */
  def build(config: CycleConfig, given: Option[Validation], useAmfJsonldSerialisation: Boolean): Future[BaseUnit] = {
    val validation: Future[Validation] = given match {
      case Some(validation: Validation) => Future { validation }
      case None                         => Validation(platform).map(_.withEnabledValidation(false))
    }
    validation.flatMap { v =>
      var options = if (!useAmfJsonldSerialisation) { ParsingOptions().withoutAmfJsonLdSerialization } else {
        ParsingOptions().withAmfJsonLdSerialization
      }
      options = options.withBaseUnitUrl("file://" + config.goldenPath)
      AMFCompiler(s"file://${config.sourcePath}", platform, config.hint, v, parsingOptions = options).build()
    }
  }

  /** Method for transforming parsed unit. Override if necessary. */
  def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = unit

  /** Method to render parsed unit. Override if necessary. */
  def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    val target  = config.target
    var options = RenderOptions().withSourceMaps.withPrettyPrint
    options =
      if (!useAmfJsonldSerialization) options.withoutAmfJsonLdSerialization else options.withAmfJsonLdSerialization
    new AMFRenderer(unit, target, options, config.syntax).renderToString
  }

  /** Method for transforming parsed unit. Override if necessary. */
  def transformRdf(unit: BaseUnit, config: CycleConfig): RdfModel = {
    unit.toNativeRdfModel()
  }

  /** Method for transforming parsed unit. Override if necessary. */
  def transformThroughRdf(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    val rdfModel = unit.toNativeRdfModel(RenderOptions().withSourceMaps)
    BaseUnit.fromNativeRdfModel(unit.id, rdfModel)
  }

  /** Method to render parsed unit. Override if necessary. */
  def renderRdf(unit: RdfModel, config: CycleConfig): Future[String] = {
    Future {
      unit.toN3().split("\n").sorted.mkString("\n")
    }
  }

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  def cycleRdf(source: String,
               golden: String,
               hint: Hint,
               target: Vendor = Amf,
               directory: String = basePath,
               validation: Option[Validation] = None,
               syntax: Option[Syntax] = None,
               pipeline: Option[String] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory, syntax, pipeline)

    build(config, validation, useAmfJsonldSerialisation = true)
      .map(transformRdf(_, config))
      .flatMap(renderRdf(_, config))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  case class CycleConfig(source: String,
                         golden: String,
                         hint: Hint,
                         target: Vendor,
                         directory: String,
                         syntax: Option[Syntax],
                         pipeline: Option[String]) {
    val sourcePath: String = directory + source
    val goldenPath: String = directory + golden
  }

  def cycleFullRdf(source: String,
                   golden: String,
                   hint: Hint,
                   target: Vendor = Amf,
                   directory: String = basePath,
                   validation: Option[Validation] = None,
                   syntax: Option[Syntax] = None,
                   pipeline: Option[String] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory, syntax, pipeline)

    build(config, validation, useAmfJsonldSerialisation = true)
      .map(transformThroughRdf(_, config))
      .flatMap(render(_, config, useAmfJsonldSerialization = true))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }
}
