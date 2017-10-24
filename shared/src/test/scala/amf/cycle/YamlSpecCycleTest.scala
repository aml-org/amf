package amf.cycle

import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.io.TmpTests
import amf.remote._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class YamlSpecCycleTest extends AsyncFunSuite with TmpTests {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/yaml/"

  test("Example 5.3 - Block Structure Indicators") {
    cycle("example-5.3.raml", "example-5.3.raml.raml", RamlYamlHint, Raml)
  }

  /** Compile source with specified hint. Dump to target and assert against same source file. */
  def cycle(source: String, hint: Hint, target: Vendor): Future[Assertion] = cycle(source, source, hint, target)

  /** Compile source with specified hint. Dump to target and assert against golden. */
  def cycle(source: String,
            golden: String,
            hint: Hint,
            target: Vendor,
            directory: String = basePath): Future[Assertion] = {
    AMFCompiler(directory + source, platform, hint)
      .build()
      .flatMap(new AMFDumper(_, target, target.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString)
      .flatMap(content => {
        val file = tmp(golden + ".tmp")
        platform.write("file://" + file, content).map((_, content))
      })
      .flatMap({
        case (path, actual) =>
          platform
            .resolve(directory + golden, None)
            .map(expected => checkDiff(actual, path, expected.stream.toString, expected.url))
      })
  }
}
