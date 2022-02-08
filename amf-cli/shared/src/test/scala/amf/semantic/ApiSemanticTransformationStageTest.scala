package amf.semantic

import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class ApiSemanticTransformationStageTest extends AsyncFunSuite with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  private val basePath = "file://amf-cli/shared/src/test/resources/semantic/"

  test("Apply semantic extension to RAML 1.0") {
    assertModel("dialect.yaml", "api.raml") { lookupPagination }
  }

  test("Apply semantic extension to OAS 2.0") {
    assertModel("dialect.yaml", "api.oas20.yaml") { lookupPagination }
  }

  test("Apply semantic extension to OAS 3.0") {
    assertModel("dialect.yaml", "api.oas30.yaml") { lookupPagination }
  }

  test("Apply semantic extension to ASYNC 2.0") {
    assertModel("dialect.yaml", "api.async.yaml") { lookupPagination }
  }

  private def lookupPagination(document: Document) = {
    val extension =
      document.encodes.asInstanceOf[Api].endPoints.head.operations.head.responses.head

    extension.graph.containsProperty("http://a.ml/vocab#pagination") shouldBe true
    extension.graph
      .getObjectByProperty("http://a.ml/vocab#pagination")
      .head
      .graph
      .scalarByProperty("http://a.ml/vocab#PageSize")
      .head shouldBe 5
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
