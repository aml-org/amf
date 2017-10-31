package amf.client

import amf.common.Tests._
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote.{Amf, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectLoaderTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  implicit override def executionContext: ExecutionContext =
    scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  test("Load Dialect from yaml") {
    println("Load dialect from yaml before validation: " + Validation.currentValidation)
    val l = platform.dialectsRegistry
    val expected = platform
      .resolve("file://shared/src/test/resources/vocabularies/muleconfig.json", None)
      .map(_.stream.toString)

    val actual = l
      .registerDialect("file://shared/src/test/resources/vocabularies/mule_config_dialect2.raml")
      .flatMap(
        x =>
          AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig.raml",
                      platform,
                      RamlYamlHint,
                      None,
                      None,
                      l).build())
      .flatMap { u =>
        val string = new AMFDumper(u, Amf, Json, GenerationOptions()).dumpToString
        println("Load dialect from yaml after validation: " + Validation.currentValidation)
        string
      }

    actual.zip(expected).map(checkDiff)

  }

}
