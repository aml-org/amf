package amf.client

import amf.common.Tests._
import amf.compiler.AMFCompiler
import amf.dialects.JSDialectRegistry
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote.{Amf, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectLoaderTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  implicit override def executionContext: ExecutionContext =
    scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  test("Load Dialect from yaml") {
    val l = platform.dialectsRegistry
    val expected = platform
      .resolve("file://shared/src/test/resources/vocabularies/muleconfig.json", None)
      .map(_.stream.toString)

    val actual = l.registerDialect("file://shared/src/test/resources/vocabularies/mule_config_dialect2.raml").flatMap( x =>
      AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig.raml", platform, RamlYamlHint,None,None,l).build()
    ).flatMap { u =>
      new AMFDumper(u, Amf, Json, GenerationOptions()).dumpToString
    }

    actual.zip(expected).map(checkDiff)
  }

}
