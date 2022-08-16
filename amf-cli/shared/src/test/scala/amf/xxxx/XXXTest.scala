package amf.xxxx

import amf.apicontract.client.scala.OASConfiguration
import amf.apicontract.client.scala.model.domain.Request
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.cache.CustomUnitCache
import amf.core.client.scala.config.CachedReference
import amf.core.client.scala.errorhandling.{ErrorHandlerProvider, UnhandledErrorHandler}
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.Linkable
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.Future

class XXXTest extends AsyncFunSuite with Matchers {

  private val basePath: String = "file://amf-cli/shared/src/test/resources/xxxx/"

  test("XXXXX") {
    val componentPath                      = "component.yaml"
    val errorHandler: ErrorHandlerProvider = () => UnhandledErrorHandler
    for {
      component <- withComponent(componentPath)
      doc <- {
        val cache = CustomUnitCache(List(CachedReference(computePath("aComponent.yaml"), component)))
        OASConfiguration
          .OAS30()
          .withUnitCache(cache)
          .withErrorHandlerProvider(errorHandler)
          .baseUnitClient()
          .parseDocument(computePath("api.yaml"))
      }
    } yield {
      all(doc.document.declares.map(_.asInstanceOf[Linkable].isLink)) shouldBe true
      all(
        doc.document.declares
          .filter(!_.isInstanceOf[Request])
          .map(_.asInstanceOf[Linkable].linkTarget.get.location().get)
      ) should include("component.yaml")
    }
  }

  private def withComponent(uri: String): Future[Module] = {
    val errorHandler: ErrorHandlerProvider = () => UnhandledErrorHandler
    OASConfiguration
      .OAS30()
      .withErrorHandlerProvider(errorHandler)
      .baseUnitClient()
      .parseDocument(basePath + uri)
      .map(_.document)
      .map(doc =>
        Module().withId(doc.id).withProcessingData(doc.processingData).withDeclares(doc.declares).withRoot(true)
      )
  }

  protected def computePath(ref: String) = {
    if (basePath.startsWith("file://")) basePath + ref
    else s"file://${basePath}" + ref
  }
}
