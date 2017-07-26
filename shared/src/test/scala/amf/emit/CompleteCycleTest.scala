package amf.emit

import amf.compiler.AMFCompiler
import amf.document.Document
import amf.domain.WebApi
import amf.dumper.AMFDumper
import amf.remote._
import amf.unsafe.PlatformSecrets
import org.scalatest.{Assertion, AsyncFunSuite}
import org.scalatest.Matchers._

import scala.concurrent.{ExecutionContext, Future}

class CompleteCycleTest extends AsyncFunSuite with PlatformSecrets {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath = "file://shared/src/test/resources/upanddown/"

  test("basic raml to oas test") {
    assertCycle(basePath + "basic.raml", basePath + "basic.json", RamlYamlHint, Oas)
  }

  test("basic oas to raml test") {
    assertCycle(basePath + "basic.json", basePath + "basic.raml", OasJsonHint, Raml)
  }

  test("basic raml to raml test") {
    assertCycle(basePath + "basic.raml", basePath + "basic.raml", RamlYamlHint, Raml)
  }

  test("basic oas to oastest") {
    assertCycle(basePath + "basic.json", basePath + "basic.json", OasJsonHint, Oas)
  }

  test("complete raml to oas test") {
    assertCycle(basePath + "complete.raml", basePath + "complete.json", RamlYamlHint, Oas)
  }

  test("complete oas to raml test") {
    assertCycle(basePath + "complete.json", basePath + "complete.raml", OasJsonHint, Raml)
  }

  test("complete raml to raml test") {
    assertCycle(basePath + "complete.raml", basePath + "complete.raml", RamlYamlHint, Raml)
  }

  test("complete oas to oas test") {
    assertCycle(basePath + "complete.json", basePath + "complete.json", OasJsonHint, Oas)
  }

  test("complete with endpoints raml to oas test") {
    assertCycle(basePath + "completeWithEndpoints.raml", basePath + "completeWithEndpoints.json", RamlYamlHint, Oas)
  }

  test("complete with endpoints raml to raml test") {
    assertCycle(basePath + "completeWithEndpoints.raml", basePath + "completeWithEndpoints.raml", RamlYamlHint, Raml)
  }

  test("complete with endpoints oas to raml test") {
    assertCycle(basePath + "completeWithEndpoints.json",
                basePath + "completeWithEndpointsPlain.raml",
                OasJsonHint,
                Raml)
  }

  test("complete with endpoints oas to oas test") {
    assertCycle(basePath + "completeWithEndpoints.json", basePath + "completeWithEndpoints.json", OasJsonHint, Oas)
  }

  //---operations

  test("complete with operations raml to oas test") {
    assertCycle(basePath + "completeWithOperations.raml", basePath + "completeWithOperations.json", RamlYamlHint, Oas)
  }

  test("complete with operations raml to raml test") {
    assertCycle(basePath + "completeWithOperations.raml", basePath + "completeWithOperations.raml", RamlYamlHint, Raml)
  }

  test("complete with operations oas to raml test") {
    assertCycle(basePath + "completeWithOperations.json",
                basePath + "completeWithOperationsPlain.raml",
                OasJsonHint,
                Raml)
  }

  test("complete with operations oas to oas test") {
    assertCycle(basePath + "completeWithOperations.json", basePath + "completeWithOperations.json", OasJsonHint, Oas)
  }

  def assertCycle(source: String, golden: String, hint: Hint, target: Vendor): Future[Assertion] = {
    val expected = platform
      .resolve(golden, None)
      .map(_.stream.toString)

    val actual = AMFCompiler(source, platform, hint)
      .build()
      .map(new AMFDumper(_, target).dump)

    actual
      .zip(expected)
      .map({
        case (dump, exp) => dump should be(exp)
      })
  }
}
