package amf.io

import amf.core.emitter.RenderOptions
import amf.common.Tests.checkDiff
import amf.core.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, AMFRenderer, Validation}
import amf.core.model.document.BaseUnit
import amf.core.remote.{Hint, Vendor}
import org.mulesoft.common.io.{AsyncFile, FileSystem}
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Cycle tests using temporary file and directory creator
  */
trait BuildCycleTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  protected val fs: FileSystem = platform.fs

  val basePath: String

  /** Return random temporary file name for testing. */
  def tmp(name: String = ""): String = platform.tmpdir() + System.nanoTime() + "-" + name

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint): Future[Assertion] = cycle(source, hint, basePath)

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, directory: String): Future[Assertion] =
    cycle(source, source, hint, hint.vendor, directory, None)

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  final def cycle(source: String,
                  golden: String,
                  hint: Hint,
                  target: Vendor,
                  directory: String = basePath,
                  validation: Option[Validation] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory)

    build(config, validation)
      .map(transform(_, config))
      .flatMap(render(_, config))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(_, config.goldenPath))
  }

  /** Method to parse unit. Override if necessary. */
  def build(config: CycleConfig, given: Option[Validation]): Future[BaseUnit] = {
    val validation: Future[Validation] = given match {
      case Some(validation: Validation) => Future { validation }
      case None                         => Validation(platform).map(_.withEnabledValidation(false))
    }
    validation.flatMap { v =>
      AMFCompiler(s"file://${config.sourcePath}", platform, config.hint, v).build()
    }
  }

  /** Method for transforming parsed unit. Override if necessary. */
  def transform(unit: BaseUnit, config: CycleConfig): BaseUnit = {
    unit
  }

  /** Method to render parsed unit. Override if necessary. */
  def render(unit: BaseUnit, config: CycleConfig): Future[String] = {
    val target = config.target
    new AMFRenderer(unit, target, target.defaultSyntax, RenderOptions().withSourceMaps).renderToString
  }

  protected def writeTemporaryFile(golden: String)(content: String): Future[AsyncFile] = {
    val actual = fs.asyncFile(tmp(s"$golden.tmp"))
    actual.write(content).map(_ => actual)
  }

  protected def assertDifferences(actual: AsyncFile, golden: String): Future[Assertion] = {
    val expected = fs.asyncFile(golden)
    expected.read().flatMap(_ => checkDiff(actual, expected))
  }

  case class CycleConfig(source: String, golden: String, hint: Hint, target: Vendor, directory: String) {
    val sourcePath: String = directory + source
    val goldenPath: String = directory + golden
  }
}
