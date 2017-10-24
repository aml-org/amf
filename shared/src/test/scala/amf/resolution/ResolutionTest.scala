package amf.resolution

import amf.ProfileNames
import amf.client.GenerationOptions
import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.io.TmpTests
import amf.remote._
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

abstract class ResolutionTest extends AsyncFunSuite with TmpTests {
  val basePath: String

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

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
      .map { model =>
        target match {
          case Raml    => model.resolve(ProfileNames.RAML)
          case Oas     => model.resolve(ProfileNames.OAS)
          case Amf     => model.resolve(ProfileNames.AMF)
          case Unknown => throw new Exception("Cannot resolve unknown fragment")
        }
      }
      .flatMap(new AMFDumper(_, Amf, Amf.defaultSyntax, GenerationOptions().withSourceMaps).dumpToString)
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
