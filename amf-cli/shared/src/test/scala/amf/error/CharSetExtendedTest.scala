package amf.error

import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.scala.model.document.Document
import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.ExecutionContext

class CharSetExtendedTest extends AsyncFunSuite with PlatformSecrets with Matchers {
  val basePath                                             = "file://amf-cli/shared/src/test/resources/parser-results/charset/"
  implicit override def executionContext: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  test("test parse UTF-8 with BOM") {
    checkContent("utf-8-bom.json")
  }

  test("test parse yaml UTF-8 with BOM") {
    checkContent("utf-8-bom.yaml")
  }

  private def checkContent(file: String) = {
    APIConfiguration.API().baseUnitClient().parse(basePath + file).map { r =>
      r.conforms shouldBe (true)
      r.baseUnit.isInstanceOf[Document] shouldBe (true)
    }
  }

}
