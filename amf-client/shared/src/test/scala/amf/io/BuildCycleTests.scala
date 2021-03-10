package amf.io

import amf.client.parse.DefaultParserErrorHandler
import amf.core.client.ParsingOptions
import amf.core.emitter.RenderOptions
import amf.core.model.document.BaseUnit
import amf.core.parser.errorhandler.{ParserErrorHandler, UnhandledParserErrorHandler}
import amf.core.rdf.RdfModel
import amf.core.remote.Syntax.Syntax
import amf.core.remote.{Amf, Hint, Vendor}
import amf.emit.AMFRenderer
import amf.facades.{AMFCompiler, Validation}
import amf.plugins.document.graph.{EmbeddedForm, FlattenedForm, JsonLdDocumentForm}
import org.scalactic.Fail
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Cycle tests using temporary file and directory creator
  */
trait JsonLdSerializationSuite {
  def testedForms: Seq[JsonLdDocumentForm] = Seq(FlattenedForm, EmbeddedForm)

  def defaultRenderOptions: RenderOptions = RenderOptions()

  def renderOptionsFor(documentForm: JsonLdDocumentForm): RenderOptions = {
    documentForm match {
      case FlattenedForm => defaultRenderOptions.withFlattenedJsonLd
      case EmbeddedForm  => defaultRenderOptions.withoutFlattenedJsonLd
      case _             => defaultRenderOptions

    }
  }
}

abstract class MultiJsonldAsyncFunSuite extends AsyncFunSuite with JsonLdSerializationSuite {

  private def validatePattern(pattern: String, patternName: String): Unit = {
    if (!pattern.contains("%s")) {
      Fail(s"$pattern is not a valid $patternName pattern. Must contain %s as the handled JSON-LD extension")
    }
  }

  // Single source, multiple JSON-LD outputs
  def multiGoldenTest(testText: String, goldenNamePattern: String, ignored: Boolean = false)(
      testFn: MultiGoldenTestConfig => Future[Assertion]): Unit = {
    testedForms.foreach { form =>
      validatePattern(goldenNamePattern, "goldenNamePattern")
      val golden = goldenNamePattern.format(form.extension)
      val config = MultiGoldenTestConfig(golden, renderOptionsFor(form))
      if (ignored) {
        ignore(s"$testText for ${form.name} JSON-LD golden")(testFn(config))
      } else {
        test(s"$testText for ${form.name} JSON-LD golden")(testFn(config))
      }

    }
  }

  // Multiple JSON-LD sources, single output
  def multiSourceTest(testText: String, sourceNamePattern: String, ignored: Boolean = false)(
      testFn: MultiSourceTestConfig => Future[Assertion]): Unit = {
    testedForms.foreach { form =>
      validatePattern(sourceNamePattern, "sourceNamePattern")
      val source = sourceNamePattern.format(form.extension)
      val config = MultiSourceTestConfig(source)
      if (ignored) {
        ignore(s"$testText for ${form.name} JSON-LD source")(testFn(config))
      } else {
        test(s"$testText for ${form.name} JSON-LD source")(testFn(config))
      }

    }
  }

  // Multiple JSON-LD sources, multiple JSON-LD outputs. Each source matches exactly one output
  def multiTest(testText: String, sourceNamePattern: String, goldenNamePattern: String, ignored: Boolean = false)(
      testFn: MultiTestConfig => Future[Assertion]): Unit = {
    testedForms.foreach { form =>
      validatePattern(sourceNamePattern, "sourceNamePattern")
      validatePattern(goldenNamePattern, "goldenNamePattern")
      val source = sourceNamePattern.format(form.extension)
      val golden = goldenNamePattern.format(form.extension)
      val config = MultiTestConfig(source, golden, renderOptionsFor(form))
      if (ignored) {
        ignore(s"$testText for ${form.name} JSON-LD")(testFn(config))
      } else {
        test(s"$testText for ${form.name} JSON-LD")(testFn(config))
      }

    }
  }
}

case class MultiGoldenTestConfig(golden: String, renderOptions: RenderOptions)
case class MultiSourceTestConfig(source: String)
case class MultiTestConfig(source: String, golden: String, renderOptions: RenderOptions)

abstract class FunSuiteCycleTests extends MultiJsonldAsyncFunSuite with BuildCycleTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}

abstract class FunSuiteRdfCycleTests extends MultiJsonldAsyncFunSuite with BuildCycleRdfTests {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
}

trait BuildCycleTestCommon extends FileAssertionTest {

  protected implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  def basePath: String

  case class CycleConfig(source: String,
                         golden: String,
                         hint: Hint,
                         target: Vendor,
                         directory: String,
                         syntax: Option[Syntax],
                         pipeline: Option[String],
                         transformWith: Option[Vendor] = None) {
    val sourcePath: String = directory + source
    val goldenPath: String = directory + golden
  }

  /** Method to parse unit. Override if necessary. */
  def build(config: CycleConfig,
            eh: Option[ParserErrorHandler],
            useAmfJsonldSerialisation: Boolean): Future[BaseUnit] = {
    Validation(platform).flatMap { _ =>
      var options =
        if (!useAmfJsonldSerialisation) ParsingOptions().withoutAmfJsonLdSerialization
        else ParsingOptions().withAmfJsonLdSerialization

      options = options.withBaseUnitUrl("file://" + config.goldenPath)

      AMFCompiler(s"file://${config.sourcePath}",
                  platform,
                  config.hint,
                  eh = eh.getOrElse(UnhandledParserErrorHandler),
                  parsingOptions = options).build()
    }
  }

  /** Method to render parsed unit. Override if necessary. */
  def render(unit: BaseUnit, config: CycleConfig, useAmfJsonldSerialization: Boolean): Future[String] = {
    val target  = config.target
    var options = RenderOptions().withSourceMaps.withPrettyPrint
    options =
      if (!useAmfJsonldSerialization) options.withoutAmfJsonLdSerialization else options.withAmfJsonLdSerialization
    new AMFRenderer(unit, target, options, config.syntax).renderToString
  }

  /** Method to render parsed unit. Override if necessary. */
  def render(unit: BaseUnit, config: CycleConfig, options: RenderOptions): Future[String] = {
    val target = config.target
    new AMFRenderer(unit, target, options, config.syntax).renderToString
  }
}

trait BuildCycleTests extends BuildCycleTestCommon {

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, syntax: Option[Syntax]): Future[Assertion] =
    cycle(source, hint, basePath, syntax)

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint): Future[Assertion] = cycle(source, hint, basePath, None)

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, directory: String, syntax: Option[Syntax]): Future[Assertion] =
    cycle(source, source, hint, hint.vendor, directory, syntax = syntax, eh = None)

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, directory: String): Future[Assertion] =
    cycle(source, source, hint, hint.vendor, directory, eh = None)

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  final def cycle(source: String,
                  golden: String,
                  hint: Hint,
                  target: Vendor,
                  directory: String = basePath,
                  renderOptions: Option[RenderOptions] = None,
                  useAmfJsonldSerialization: Boolean = true,
                  syntax: Option[Syntax] = None,
                  pipeline: Option[String] = None,
                  transformWith: Option[Vendor] = None,
                  eh: Option[ParserErrorHandler] = None): Future[Assertion] = {

    val config                 = CycleConfig(source, golden, hint, target, directory, syntax, pipeline, transformWith)
    val amfJsonLdSerialization = renderOptions.map(_.isAmfJsonLdSerilization).getOrElse(useAmfJsonldSerialization)

    build(config, eh.orElse(Some(DefaultParserErrorHandler.withRun())), amfJsonLdSerialization)
      .map(transform(_, config))
      .flatMap {
        renderOptions match {
          case Some(options) => render(_, config, options)
          case None          => render(_, config, useAmfJsonldSerialization)
        }
      }
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  /** Method for transforming parsed unit. Override if necessary. */
  def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = unit
}

trait BuildCycleRdfTests extends BuildCycleTestCommon {

  def cycleFullRdf(source: String,
                   golden: String,
                   hint: Hint,
                   target: Vendor = Amf,
                   directory: String = basePath,
                   renderOptions: Option[RenderOptions] = None,
                   syntax: Option[Syntax] = None,
                   pipeline: Option[String] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory, syntax, pipeline, None)

    build(config, None, useAmfJsonldSerialisation = true)
      .map(transformThroughRdf(_, config))
      .flatMap {
        renderOptions match {
          case Some(options) => render(_, config, options)
          case None          => render(_, config, useAmfJsonldSerialization = true)
        }
      }
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  def cycleRdf(source: String,
               golden: String,
               hint: Hint,
               target: Vendor = Amf,
               directory: String = basePath,
               syntax: Option[Syntax] = None,
               pipeline: Option[String] = None,
               transformWith: Option[Vendor] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory, syntax, pipeline, transformWith)

    build(config, None, useAmfJsonldSerialisation = true)
      .map(transformRdf(_, config))
      .flatMap(renderRdf(_, config))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
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
}
