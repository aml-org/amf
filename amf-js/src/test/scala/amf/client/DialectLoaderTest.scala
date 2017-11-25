package amf.client

import amf.common.Tests._
import amf.core.client.GenerationOptions
import amf.framework.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, AMFDumper, Validation}
import amf.framework.remote.Syntax.Json
import amf.framework.remote.{Amf, RamlYamlHint}
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectLoaderTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest {
  implicit override def executionContext: ExecutionContext =
    scala.scalajs.concurrent.JSExecutionContext.Implicits.queue

  test("Load Dialect from yaml") {
    val dialectsRegistry = platform.dialectsRegistry
    val expected = platform
      .resolve("file://shared/src/test/resources/vocabularies/muleconfig.json", None)
      .map(_.stream.toString)

    val actual = dialectsRegistry
      .registerDialect("file://shared/src/test/resources/vocabularies/mule_config_dialect2.raml")
      .flatMap(
        x =>
          AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig.raml",
                      platform,
                      RamlYamlHint,
                      Validation(platform),
                      None,
                      None).build())
      .map(new AMFDumper(_, Amf, Json, GenerationOptions()).dumpToString)

    actual.zip(expected).map(checkDiff)

  }

}
