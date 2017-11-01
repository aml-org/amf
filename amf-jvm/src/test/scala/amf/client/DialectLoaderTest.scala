package amf.client

import amf.common.AmfObjectTestMatcher
import amf.common.Tests._
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote.{Amf, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import amf.validation.Validation
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectLoaderTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Load Dialect from yaml") {
    val l = platform.dialectsRegistry

    val expected = platform
      .resolve("file://shared/src/test/resources/vocabularies/muleconfig.json", None)
      .map(_.stream.toString)

    val actual = l.registerDialect("file://shared/src/test/resources/vocabularies/mule_config_dialect2.raml").flatMap( x =>
      AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig.raml", platform, RamlYamlHint, Validation(platform), None, None, l).build()
    ).flatMap { u =>
      val encoded = u.asInstanceOf[amf.document.Document].encodes
      assert(encoded.getTypeIds().length == 2)
      new AMFDumper(u, Amf, Json, GenerationOptions()).dumpToString
    }

    actual.zip(expected).map(checkDiff)
  }
}
