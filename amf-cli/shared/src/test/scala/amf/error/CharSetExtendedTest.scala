package amf.error

import amf.apicontract.client.scala.APIConfiguration
import amf.core.client.common.remote.Content
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.resource.ResourceLoader
import amf.core.internal.unsafe.PlatformSecrets
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

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
    APIConfiguration.API().withResourceLoaders(List(BomResourceLoader)).baseUnitClient().parse(basePath + file).map {
      r =>
        r.conforms shouldBe (true)
        r.baseUnit.isInstanceOf[Document] shouldBe true
    }
  }

  object BomResourceLoader extends ResourceLoader {

    private val BOM = 0xFEFF.toChar

    override def fetch(resource: String): Future[Content] =
      platform.fs
        .asyncFile(resource.replace("file://", ""))
        .read()
        .map(x => prependBomIfDoesntExist(x.toString))
        .map(x => new Content(x, resource))

    private def prependBomIfDoesntExist(content: String): String = if (content.head == BOM) content else BOM + content

    override def accepts(resource: String): Boolean = true
  }
}
