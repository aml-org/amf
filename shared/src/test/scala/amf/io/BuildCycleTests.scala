package amf.io

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.{Hint, Vendor}
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import org.mulesoft.common.io.FileSystem
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Cycle tests using temporary file and directory creator
  */
trait BuildCycleTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val fs: FileSystem = platform.fs

  val basePath: String

  /** Return random temporary file name for testing. */
  def tmp(name: String = ""): String = platform.tmpdir() + System.nanoTime() + "-" + name

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint): Future[Assertion] = cycle(source, source, hint, hint.vendor, basePath, None)

  /** Compile source with specified hint. Render to temporary file and assert against golden. */
  final def cycle(source: String,
                  golden: String,
                  hint: Hint,
                  target: Vendor,
                  directory: String = basePath,
                  validation: Option[Validation] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory)

    build(config, validation)
      .map(map(_, config))
      .flatMap(render(_, config))
      .flatMap(writeTemporaryFile(golden))
      .flatMap(assertDifferences(config))
  }

  def build(config: CycleConfig, given: Option[Validation]): Future[BaseUnit] = {
    val validation = given.getOrElse(Validation(platform).withEnabledValidation(false))
    AMFCompiler("file://" + config.sourcePath, platform, config.hint, validation).build()
  }

  def map(unit: BaseUnit, config: CycleConfig): BaseUnit = unit

  def render(unit: BaseUnit, config: CycleConfig): Future[String] = {
    val target = config.target
    new AMFDumper(unit, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString
  }

  private def writeTemporaryFile(golden: String)(content: String): Future[(String, String)] = {
    val path = tmp(golden + ".tmp")
    fs.asyncFile(path).write(content).map(_ => (path, content))
  }

  private def assertDifferences(config: CycleConfig)(tmp: (String, String)) = tmp match {
    case (path, actual) =>
      fs.asyncFile(config.goldenPath)
        .read()
        .map(expected => checkDiff(actual, path, expected.toString, config.goldenPath))

  }

  case class CycleConfig(source: String, golden: String, hint: Hint, target: Vendor, directory: String) {
    val sourcePath: String = directory + source
    val goldenPath: String = directory + golden
  }
}
