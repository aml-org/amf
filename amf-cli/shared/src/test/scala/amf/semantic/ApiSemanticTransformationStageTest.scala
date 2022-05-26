package amf.semantic

import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation, Response}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.DomainElement
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class ApiSemanticTransformationStageTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://amf-cli/shared/src/test/resources/semantic/"

  test("Apply semantic extension to RAML 1.0") {
    assertModel("dialect.yaml", "api.raml") { doc => lookupPagination(getResponse(doc), 5) }
  }

  test("Apply semantic extension to OAS 2.0") {
    assertModel("dialect.yaml", "api.oas20.yaml") { doc => lookupPagination(getResponse(doc), 5) }
  }

  test("Apply semantic extension to OAS 3.0") {
    assertModel("dialect.yaml", "api.oas30.yaml") { doc => lookupPagination(getResponse(doc), 5) }
  }

  test("Apply semantic extension to ASYNC 2.0") {
    assertModel("dialect.yaml", "api.async.yaml") { doc => lookupPagination(getResponse(doc), 5) }
  }

  test("Apply nested semantic extensions to RAML 1.0") {
    assertModel("dialect-endpoint-operation.yaml", "api-endpoint-operation.raml") { doc =>
      lookupPagination(getEndpoint(doc), 15)
      lookupPagination(getOperation(doc), 10)
    }
  }

  private def getEndpoint(document: Document): EndPoint = document.encodes.asInstanceOf[Api].endPoints.head
  private def getOperation(document: Document): Operation =
    document.encodes.asInstanceOf[Api].endPoints.head.operations.head
  private def getResponse(document: Document): Response =
    document.encodes.asInstanceOf[Api].endPoints.head.operations.head.responses.head

  private def lookupPagination(element: DomainElement, value: Int): Assertion = {

    element.graph.containsProperty("http://a.ml/vocab#pagination") shouldBe true
    element.graph
      .getObjectByProperty("http://a.ml/vocab#pagination")
      .head
      .graph
      .scalarByProperty("http://a.ml/vocab#PageSize")
      .head shouldBe value
  }

  private def assertModel(dialect: String, api: String)(assertion: Document => Assertion): Future[Assertion] = {
    val config = APIConfiguration
      .API()
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(basePath + dialect)
    for {
      nextConfig  <- config
      parseResult <- nextConfig.baseUnitClient().parseDocument(basePath + api)
    } yield {
      val transformConfig = APIConfiguration.fromSpec(parseResult.document.sourceSpec.get)
      val transformed     = transformConfig.baseUnitClient().transform(parseResult.document)
      assertion(transformed.baseUnit.asInstanceOf[Document])
    }
  }
}
