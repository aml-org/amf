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

  /** Compile source with specified hint. Dump to target and assert against golden. */
  def cycle(source: String,
            golden: String,
            hint: Hint,
            target: Vendor,
            directory: String = basePath,
            maybeValidation: Option[Validation] = None): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory)

    build(config, maybeValidation)
      .map(map(_, config))
      .flatMap(render(_, config))
      .flatMap(content => {
        val path = tmp(golden + ".tmp")
        fs.asyncFile(path).write(content).map(_ => (path, content))
      })
      .flatMap({
        case (path, actual) =>
          fs.asyncFile(config.goldenPath)
            .read()
            .map(expected => checkDiff(actual, path, expected.toString, config.goldenPath))
      })
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

  case class CycleConfig(source: String, golden: String, hint: Hint, target: Vendor, directory: String) {
    val sourcePath: String = directory + source
    val goldenPath: String = directory + golden
  }
}
