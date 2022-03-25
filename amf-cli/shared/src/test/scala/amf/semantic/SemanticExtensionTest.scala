package amf.semantic

import amf.aml.client.scala.model.document.Dialect
import amf.aml.client.scala.{AMLConfiguration, AMLDialectResult}
import amf.apicontract.client.scala.{AMFConfiguration, AMFLibraryResult, APIConfiguration}
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.config.{CachedReference, UnitCache}
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Value
import amf.core.internal.remote.{AsyncApi20, Oas20, Oas30, Raml10, Spec}
import org.mulesoft.antlrast.unsafe.PlatformSecrets
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import amf.core.client.scala.model.document.Module

import scala.concurrent.{ExecutionContext, Future}

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

  private def lookupOperation(document: Document) = {
    val extension =
      document.encodes.asInstanceOf[Api].endPoints.head.operations.head.customDomainProperties.head

    assertPaginationExtension(extension, 10)
  }
}
