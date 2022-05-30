package amf.semantic

import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.model.document.Document
import amf.core.internal.remote.{AsyncApi20, Oas20, Oas30, Raml10}
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class SemanticExtensionTest extends AsyncFunSuite with SemanticExtensionParseTest with Matchers {

  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  override protected val basePath = "file://amf-cli/shared/src/test/resources/semantic/"

  test("Apply semantic extension to RAML 1.0") {
    assertModel("dialect.yaml", "api.raml", Raml10) { lookupResponse }
  }

  test("Apply semantic extension to OAS 2.0") {
    assertModel("dialect.yaml", "api.oas20.yaml", Oas20) { lookupResponse }
  }

  test("Apply semantic extension to OAS 3.0") {
    assertModel("dialect.yaml", "api.oas30.yaml", Oas30) { lookupResponse }
  }

  test("Apply semantic extension to ASYNC 2.0") {
    assertModel("dialect.yaml", "api.async.yaml", AsyncApi20) { lookupResponse }
  }

  test("Apply same SemEx to Request and Response") {
    assertModel("dialect-several-domains.yaml", "api-several-domains.raml", Raml10) { doc =>
      lookupResponse(doc)
      lookupOperation(doc)
    }
  }

  test("Apply same SemEx to Endpoint and Operation") {
    assertModel("dialect-endpoint-operation.yaml", "api-endpoint-operation.raml", Raml10) { doc =>
      lookupEndpoint(doc)
      lookupOperation(doc)
    }
  }

  private def lookupOperation(document: Document) = {
    val extension =
      document.encodes.asInstanceOf[Api].endPoints.head.operations.head.customDomainProperties.head

    assertPaginationExtension(extension, 10)
  }

  private def lookupEndpoint(document: Document) = {
    val extension =
      document.encodes.asInstanceOf[Api].endPoints.head.customDomainProperties.head

    assertPaginationExtension(extension, 15)
  }
}
