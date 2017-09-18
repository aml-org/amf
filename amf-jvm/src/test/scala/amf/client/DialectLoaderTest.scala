package amf.client

import amf.common.AmfObjectTestMatcher
import amf.common.Tests._
import amf.compiler.AMFCompiler
import amf.dialects.JVMDialectRegistry
import amf.dumper.AMFDumper
import amf.remote.Syntax.Json
import amf.remote.{Amf, RamlYamlHint}
import amf.unsafe.PlatformSecrets
import org.scalatest.AsyncFunSuite

import scala.concurrent.ExecutionContext

class DialectLoaderTest extends AsyncFunSuite with PlatformSecrets with PairsAMFUnitFixtureTest with AmfObjectTestMatcher {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Load Dialect from yaml") {
    val l=new JVMDialectRegistry();
    val expected = platform
      .resolve("file://shared/src/test/resources/vocabularies/muleconfig.json", None)
      .map(_.stream.toString)
    var actual=l.add(platform,"file://shared/src/test/resources/vocabularies/mule_config_dialect2.raml").flatMap(
      (x)=>AMFCompiler("file://shared/src/test/resources/vocabularies/muleconfig.raml", platform, RamlYamlHint,None,None,l).build()
    ).flatMap(u=>new AMFDumper(u, Amf, Json, GenerationOptions()).dumpToString)
    actual.zip(expected).map(checkDiff)
  }
}
