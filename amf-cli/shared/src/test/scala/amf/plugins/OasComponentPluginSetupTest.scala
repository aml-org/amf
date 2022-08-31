package amf.plugins

import amf.apicontract.client.scala.configuration.OasComponentConfiguration
import amf.apicontract.client.scala.model.document.ComponentModule
import amf.core.client.scala.AMFParseResult
import amf.core.client.scala.exception.UnsupportedDomainForDocumentException
import amf.core.client.scala.model.document.Module
import amf.core.internal.remote.Spec
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class OasComponentPluginSetupTest extends AsyncFunSuite with Matchers {

  private val base = "file://amf-api-contract/shared/src/test/resources/oas-component/"

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("OAS 3.0 Component valid parsing YAML") {
    for {
      parsed <- parse("simple.yaml")
    } yield {
      parsed.baseUnit shouldBe a[ComponentModule]
      parsed.sourceSpec shouldBe Spec.OAS30
      parsed.conforms shouldBe true
      val lib = parsed.baseUnit.asInstanceOf[ComponentModule]
      lib.declares should have size 24
    }
  }

  test("OAS 3.0 Component valid parsing JSON") {
    for {
      parsed <- parse("simple.json")
    } yield {
      parsed.baseUnit shouldBe a[ComponentModule]
      parsed.sourceSpec shouldBe Spec.OAS30
      parsed.conforms shouldBe true
      val lib = parsed.baseUnit.asInstanceOf[ComponentModule]
      lib.declares should have size 24
    }
  }

  test("OAS 3.0 Component without OAS 3 header") {
    recoverToSucceededIf[UnsupportedDomainForDocumentException] {
      parse("invalid-no-header.yaml")
    }
  }

  private def parse(uri: String): Future[AMFParseResult] = {
    val client = OasComponentConfiguration.OAS30Component().baseUnitClient()
    client.parse(base + uri)
  }
}
