package amf.io

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.document.BaseUnit
import amf.dumper.AMFDumper
import amf.remote.{Hint, Vendor}
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Cycle tests using temporary file and directory creator
  */
trait BuildCycleTests extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath: String

  /** Return random temporary file name for testing. */
  def tmp(name: String = ""): String = platform.tmpdir() + System.nanoTime() + "-" + name

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, target: Vendor): Future[Assertion] = cycle(source, source, hint, target)

  /** Compile source with specified hint. Dump to target and assert against golden. */
  def cycle(source: String,
            golden: String,
            hint: Hint,
            target: Vendor,
            directory: String = basePath): Future[Assertion] = {

    val config = CycleConfig(source, golden, hint, target, directory)

    build(config)
      .map(map(_, config))
      .flatMap(render(_, config))
      .flatMap(content => platform.write("file://" + tmp(golden + ".tmp"), content).map((_, content)))
      .flatMap({
        case (path, actual) =>
          platform
            .resolve(config.goldenPath, None)
            .map(expected => checkDiff(actual, path, expected.stream.toString, expected.url))
      })
  }

  def build(config: CycleConfig): Future[BaseUnit] = AMFCompiler(config.sourcePath, platform, config.hint).build()

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
