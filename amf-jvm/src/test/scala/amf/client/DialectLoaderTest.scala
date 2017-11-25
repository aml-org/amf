package amf.client

import amf.common.AmfObjectTestMatcher
import amf.common.Tests._
import amf.core.client.GenerationOptions
import amf.framework.unsafe.PlatformSecrets
import amf.facades.{AMFCompiler, AMFDumper, Validation}
import amf.framework.remote.Syntax.Json
import amf.framework.remote.{Amf, RamlYamlHint}
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectLoaderTest
    extends AsyncFunSuite
    with PlatformSecrets
    with PairsAMFUnitFixtureTest
    with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Load Dialect from yaml") {
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
                      Validation(platform),
                      None,
                      None).build())
      .map { u =>
        val encoded = u.asInstanceOf[amf.framework.model.document.Document].encodes
        assert(encoded.getTypeIds().length == 2)
        new AMFDumper(u, Amf, Json, GenerationOptions()).dumpToString
      }

    actual.zip(expected).map(checkDiff)
  }
}
