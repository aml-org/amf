package amf.semantic

import amf.aml.client.scala.AMLConfiguration
import amf.aml.client.scala.model.document.DialectInstance
import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class JsonLdSemanticExtensionsParseTest extends AsyncFunSuite with Matchers {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global

  val basePath: String = "file://amf-cli/shared/src/test/resources/semantic/"

  test("Parse JSON-LD with semantic extensions for RAML 1.0") {
    assertParsedModel("dialect.yaml", "instance.raml.jsonld", lookupPagination)
  }

  test("Parse JSON-LD with semantic extensions for OAS 1.0") {
    assertParsedModel("dialect.yaml", "instance.oas20.jsonld", lookupPagination)
  }

  test("Parse JSON-LD with semantic extensions for OAS 3.0") {
    assertParsedModel("dialect.yaml", "instance.oas30.jsonld", lookupPagination)
  }

  test("Parse JSON-LD with semantic extensions for ASYNC 2.0") {
    assertParsedModel("dialect.yaml", "instance.async.jsonld", lookupPagination)
  }

  private def assertParsedModel(dialectPath: String,
                                jsonLdPath: String,
                                assertion: Document => Assertion): Future[Assertion] = {
    for {
      model <- parseJsonLd(dialectPath, jsonLdPath)
    } yield {
      assertion(model)
    }
  }

  private def parseJsonLd(dialectPath: String, jsonLdPath: String): Future[Document] = {
    for {
      config <- APIConfiguration
        .API()
        .withErrorHandlerProvider(() => UnhandledErrorHandler)
        .withDialect(s"$basePath$dialectPath")
      result <- config.baseUnitClient().parse(s"$basePath$jsonLdPath")
    } yield {
      result.baseUnit.asInstanceOf[Document]
    }
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
}
