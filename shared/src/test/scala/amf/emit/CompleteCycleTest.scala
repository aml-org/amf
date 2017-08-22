package amf.emit

import amf.common.Tests.checkDiff
import amf.compiler.AMFCompiler
import amf.dumper.AMFDumper
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}

import scala.concurrent.{ExecutionContext, Future}

class CompleteCycleTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/upanddown/"

  test("Simplest raml to raml test") {
    assertCycle("simplest.raml", "simplestModified.raml", RamlYamlHint, Raml)
  }

  test("Simplest oas to oas test") {
    assertCycle("simplest.json", "simplest.json", OasJsonHint, Oas)
  }

  test("Basic amf to amf test") {
    assertCycle("basic.jsonld", "basic.jsonld", AmfJsonLdHint, Amf)
  }

  test("Basic raml to amf test") {
    assertCycle("basic.raml", "basic.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Basic oas to amf test") {
    assertCycle("basic.json", "basic.json.jsonld", OasJsonHint, Amf)
  }

  test("Basic amf(raml) to raml test") {
    assertCycle("basic.raml.jsonld", "basic.raml", AmfJsonLdHint, Raml)
  }

  test("Basic amf(oas) to oas test") {
    assertCycle("basic.json.jsonld", "basic.json", AmfJsonLdHint, Oas)
  }

  test("Basic raml to oas test") {
    assertCycle("basic.raml", "basic.raml.json", RamlYamlHint, Oas)
  }

  test("Basic oas to raml test") {
    assertCycle("basic.json", "basic.json.raml", OasJsonHint, Raml)
  }

  test("Basic raml to raml test") {
    assertCycle("basic.raml", "basic.raml", RamlYamlHint, Raml)
  }

  test("Basic oas to oas test") {
    assertCycle("basic.json", "basic.json", OasJsonHint, Oas)
  }

  test("Complete amf to amf test") {
    assertCycle("complete.jsonld", "complete.jsonld", AmfJsonLdHint, Amf)
  }

  test("Complete raml to amf test") {
    assertCycle("complete.raml", "complete.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Complete raml to oas test") {
    assertCycle("complete.raml", "complete.json", RamlYamlHint, Oas)
  }

  test("Complete oas to raml test") {
    assertCycle("complete.json", "complete.raml", OasJsonHint, Raml)
  }

  test("Complete raml to raml test") {
    assertCycle("complete.raml", "complete.raml", RamlYamlHint, Raml)
  }

  test("Complete oas to oas test") {
    assertCycle("complete.json", "complete.json", OasJsonHint, Oas)
  }

  test("Endpoints amf to amf test") {
    assertCycle("endpoints.jsonld", "endpoints.jsonld", AmfJsonLdHint, Amf)
  }

  test("Endpoints raml to amf test") {
    assertCycle("endpoints.raml", "endpoints.raml.jsonld", RamlYamlHint, Amf)
  }

  test("Endpoints amf(raml) to raml test") {
    assertCycle("endpoints.raml.jsonld", "endpoints.raml.jsonld.raml", AmfJsonLdHint, Raml)
  }

  test("Endpoints raml to oas test") {
    assertCycle("endpoints.raml", "endpoints.json", RamlYamlHint, Oas)
  }

  test("Endpoints raml to raml test") {
    assertCycle("endpoints.raml", "endpoints.raml", RamlYamlHint, Raml)
  }

  test("Endpoints oas to raml test") {
    assertCycle("endpoints.json", "endpoints.json.raml", OasJsonHint, Raml)
  }

  test("Endpoints oas to oas test") {
    assertCycle("endpoints.json", "endpoints.json", OasJsonHint, Oas)
  }

  test("Complete with operations raml to oas test") {
    assertCycle("completeWithOperations.raml", "completeWithOperations.json", RamlYamlHint, Oas)
  }

  test("Complete with operations raml to raml test") {
    assertCycle("completeWithOperations.raml", "completeWithOperations.raml", RamlYamlHint, Raml)
  }

  test("Complete with operations oas to raml test") {
    assertCycle("completeWithOperations.json", "complete-with-operations.json.raml", OasJsonHint, Raml)
  }

  test("Complete with operations oas to oas test") {
    assertCycle("completeWithOperations.json", "completeWithOperations.json", OasJsonHint, Oas)
  }

  test("Complete with request raml to raml test") {
    assertCycle("operation-request.raml", "operation-request.raml", RamlYamlHint, Raml)
  }

  test("Complete with request oas to raml test") {
    assertCycle("operation-request.json", "operation-request.json.raml", OasJsonHint, Raml)
  }

  test("Complete with request raml to oas test") {
    assertCycle("operation-request.raml", "operation-request.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with response oas to raml test") {
    assertCycle("operation-response.json", "operation-response.raml", OasJsonHint, Raml)
  }

  test("Complete with response oas to oas test") {
    assertCycle("operation-response.json", "operation-response.json", OasJsonHint, Oas)
  }

  test("Complete with response raml to raml test") {
    assertCycle("operation-response.raml", "operation-response.raml", RamlYamlHint, Raml)
  }

  test("Complete with response raml to oas test") {
    assertCycle("operation-response.raml", "operation-response.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with payloads raml to raml test") {
    assertCycle("payloads.raml", "payloads.raml", RamlYamlHint, Raml)
  }

  test("Complete with payloads raml to oas test") {
    assertCycle("payloads.raml", "payloads.raml.json", RamlYamlHint, Oas)
  }

  test("Complete with payloads oas to oas test") {
    assertCycle("payloads.json", "payloads.json", OasJsonHint, Oas)
  }

  test("Complete with payloads oas to raml test") {
    assertCycle("payloads.json", "payloads.json.raml", OasJsonHint, Raml)
  }

  def assertCycle(source: String, golden: String, hint: Hint, target: Vendor): Future[Assertion] = {
    val expected = platform
      .resolve(basePath + golden, None)
      .map(_.stream.toString)

    val actual = AMFCompiler(basePath + source, platform, hint)
      .build()
      .flatMap(unit => new AMFDumper(unit, target).dumpToStream)

    actual
      .zip(expected)
      .map(checkDiff)
  }
}
