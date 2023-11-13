package amf.components

import amf.apicontract.client.scala.model.document.ComponentModule
import amf.apicontract.client.scala.model.domain.Request
import amf.apicontract.client.scala.{AMFDocumentResult, OASConfiguration}
import amf.cache.CustomUnitCache
import amf.core.client.scala.config.CachedReference
import amf.core.client.scala.errorhandling.{ErrorHandlerProvider, UnhandledErrorHandler}
import amf.core.client.scala.model.document.Module
import amf.core.client.scala.model.domain.{DomainElement, Linkable}
import amf.core.internal.annotations.VirtualElement
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class OasComponentLinkingTest extends AsyncFunSuite with Matchers {

  private val basePath: String = "file://amf-cli/shared/src/test/resources/components/oas3/"

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

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

  // W-14138352
  test("Oas component module with header parameter referenced in document") {
    val componentPath                      = "header-component.yaml"
    val errorHandler: ErrorHandlerProvider = () => UnhandledErrorHandler
    for {
      component <- withComponent(`componentPath`)
      doc <- {
        val cache = buildCache("header-component.yaml", component)
        getConfig(errorHandler, cache)
          .parseDocument(computePath("header-api.yaml"))
      }
    } yield {
      all(shouldBeLink(doc)) shouldBe true
      all(
        doc.document.declares.filter(!_.isInstanceOf[Request]).map(linkTarget(_).location().get)
      ) should include("header-component.yaml")
    }
  }

  // W-14138352
  test("Oas component module references another oas component response") {
    val componentPath                      = "response-component.yaml"
    val errorHandler: ErrorHandlerProvider = () => UnhandledErrorHandler
    for {
      component <- withComponent(`componentPath`)
      doc <- {
        val cache = buildCache("response-component.yaml", component)
        getComponentConfig(errorHandler, cache)
          .parseLibrary(computePath("response-api.yaml"))
      }
    } yield {
      doc.results.size shouldBe 0
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

  private def getComponentConfig(errorHandler: ErrorHandlerProvider, cache: CustomUnitCache) = {
    OASConfiguration
      .OAS30Component()
      .withUnitCache(cache)
      .withErrorHandlerProvider(errorHandler)
      .baseUnitClient()
  }

  private def withComponent(uri: String): Future[Module] = {
    val errorHandler: ErrorHandlerProvider = () => UnhandledErrorHandler
    OASConfiguration
      .OAS30Component()
      .withErrorHandlerProvider(errorHandler)
      .baseUnitClient()
      .parse(basePath + uri)
      .map(_.baseUnit.asInstanceOf[ComponentModule])
  }

  protected def computePath(ref: String): String = {
    if (basePath.startsWith("file://")) basePath + ref
    else s"file://${basePath}" + ref
  }
}
