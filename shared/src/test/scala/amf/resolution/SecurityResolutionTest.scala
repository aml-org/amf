package amf.resolution

import amf.ProfileNames
import amf.client.GenerationOptions
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.io.TmpTests
import amf.remote._
import org.scalatest.{Assertion, AsyncFunSuite}
import amf.common.Tests.checkDiff

import scala.concurrent.{ExecutionContext, Future}

class SecurityResolutionTest extends ResolutionTest {

  override val basePath = "file://shared/src/test/resources/resolution/security/"

  test("Security resolution raml to AMF") {
    cycle("security.raml", "security.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Security resolution oas to AMF") {
    cycle("security.json", "security.json.jsonld", OasJsonHint, Amf)
  }
}
