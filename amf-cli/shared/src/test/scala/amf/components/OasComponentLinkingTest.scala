package amf.components

import amf.apicontract.client.scala.{AMFDocumentResult, OASConfiguration}
import amf.apicontract.client.scala.model.domain.Request
import amf.cache.CustomUnitCache
import amf.core.client.scala.config.CachedReference
import amf.core.client.scala.errorhandling.{ErrorHandlerProvider, UnhandledErrorHandler}
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.annotations.{VirtualElement, VirtualNode}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class OasComponentLinkingTest extends AsyncFunSuite with Matchers {

  private val basePath: String = "file://amf-cli/shared/src/test/resources/components/oas3/"

  override implicit val executionContext = ExecutionContext.Implicits.global

  test("Oas component module can be used from document when injected in cache") {
    val componentPath                      = "simple-components.yaml"
    val errorHandler: ErrorHandlerProvider = () => UnhandledErrorHandler
    for {
      component <- withComponent(componentPath)
      doc <- {
        val cache = buildCache("aComponent.yaml", component)
        getConfig(errorHandler, cache)
          .parseDocument(computePath("api.yaml"))
      }
    } yield {
      all(shouldBeLink(doc)) shouldBe true
      all(
        doc.document.declares.filter(!_.isInstanceOf[Request]).map(linkTarget(_).location().get)
      ) should include("simple-components.yaml")
      linkTarget(findRequest(doc)).annotations.find(_.isInstanceOf[VirtualElement]) should not be empty
    }
  }

  private def linkTarget(elem: DomainElement) = elem.asInstanceOf[Linkable].linkTarget.get

  private def findRequest(doc: AMFDocumentResult) = doc.document.declares.find(_.isInstanceOf[Request]).get

  private def shouldBeLink(doc: AMFDocumentResult) = {
    doc.document.declares.map(_.asInstanceOf[Linkable].isLink)
  }

  private def buildCache(key: String, component: Module) = {
    CustomUnitCache(List(CachedReference(computePath(key), component)))
  }

  private def getConfig(errorHandler: ErrorHandlerProvider, cache: CustomUnitCache) = {
    OASConfiguration
      .OAS30()
      .withUnitCache(cache)
      .withErrorHandlerProvider(errorHandler)
      .baseUnitClient()
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

  protected def computePath(ref: String): String = {
    if (basePath.startsWith("file://")) basePath + ref
    else s"file://${basePath}" + ref
  }
}
