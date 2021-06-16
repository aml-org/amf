package amf.io

import amf.apicontract.client.scala.config.{AMFConfiguration, AsyncAPIConfiguration, WebAPIConfiguration}
import amf.core.client.scala.AMFGraphConfiguration
import amf.core.client.scala.config.RenderOptions
import amf.core.client.scala.errorhandling.{AMFErrorHandler, IgnoringErrorHandler}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.rdf.{RdfModel, RdfUnitConverter}
import amf.core.internal.plugins.document.graph.{EmbeddedForm, FlattenedForm, JsonLdDocumentForm}
import amf.core.internal.remote.Syntax.Syntax
import amf.core.internal.remote.{Amf, Hint, Vendor}
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

    val sourceMediaType: String = hint.vendor.mediaType + "+" + hint.syntax.extension
    val targetMediaType: String = target.mediaType + syntax.map(s => s"+${s.extension}").getOrElse("")
  }

  /** Method to parse unit. Override if necessary. */
  def build(config: CycleConfig, amfConfig: AMFGraphConfiguration): Future[BaseUnit] = {
    amfConfig
      .withParsingOptions(amfConfig.options.parsingOptions.withBaseUnitUrl("file://" + config.goldenPath))
      .createClient()
      .parse(s"file://${config.sourcePath}")
      .map(_.bu)
  }

  /** Method to render parsed unit. Override if necessary. */
  def render(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): String = {
    amfConfig.createClient().render(unit, config.targetMediaType)
  }
  def renderOptions(): RenderOptions = RenderOptions()

  protected def buildConfig(options: Option[RenderOptions], eh: Option[AMFErrorHandler]): AMFConfiguration = {
    val amfConfig: AMFConfiguration = WebAPIConfiguration.WebAPI().merge(AsyncAPIConfiguration.Async20())
    val renderedConfig: AMFConfiguration = options.fold(amfConfig.withRenderOptions(renderOptions()))(r => {
      amfConfig.withRenderOptions(r)
    })
    eh.fold(renderedConfig.withErrorHandlerProvider(() => IgnoringErrorHandler))(e =>
      renderedConfig.withErrorHandlerProvider(() => e))
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
                  syntax: Option[Syntax] = None,
                  pipeline: Option[String] = None,
                  transformWith: Option[Vendor] = None,
                  eh: Option[AMFErrorHandler] = None): Future[Assertion] = {

    val config    = CycleConfig(source, golden, hint, target, directory, syntax, pipeline, transformWith)
    val amfConfig = buildConfig(renderOptions, eh)

    for {
      parsed       <- build(config, amfConfig)
      resolved     <- Future.successful(transform(parsed, config, amfConfig))
      actualString <- Future.successful(render(resolved, config, amfConfig))
      actualFile   <- writeTemporaryFile(golden)(actualString)
      assertion    <- assertDifferences(actualFile, config.goldenPath)
    } yield {
      assertion
    }
  }

  /** Method for transforming parsed unit. Override if necessary. */
  def transform(unit: BaseUnit, config: CycleConfig, amfConfig: AMFConfiguration): BaseUnit = unit
}

trait BuildCycleRdfTests extends BuildCycleTestCommon {

  def cycleFullRdf(source: String,
                   golden: String,
                   hint: Hint,
                   target: Vendor = Amf,
                   directory: String = basePath,
                   syntax: Option[Syntax] = None,
                   pipeline: Option[String] = None): Future[Assertion] = {

    val config    = CycleConfig(source, golden, hint, target, directory, syntax, pipeline, None)
    val amfConfig = buildConfig(None, None)
    build(config, amfConfig)
      .map(transformThroughRdf(_, config))
      .map { render(_, config, amfConfig) }
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

    val config    = CycleConfig(source, golden, hint, target, directory, syntax, pipeline, transformWith)
    val amfConfig = buildConfig(None, None)
    build(config, amfConfig)
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
    new RdfUnitConverter().fromNativeRdfModel(unit.id, rdfModel, AMFGraphConfiguration.predefined())
  }

  /** Method to render parsed unit. Override if necessary. */
  def renderRdf(unit: RdfModel, config: CycleConfig): Future[String] = {
    Future {
      unit.toN3().split("\n").sorted.mkString("\n")
    }
  }
}
