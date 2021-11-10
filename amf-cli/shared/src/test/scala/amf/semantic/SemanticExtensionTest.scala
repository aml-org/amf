package amf.semantic

import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.errorhandling.UnhandledErrorHandler
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.annotations.LexicalInformation
import amf.core.internal.parser.domain.Value
import org.scalatest.{Assertion, AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class SemanticExtensionTest extends AsyncFunSuite with Matchers {

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
      document.encodes.asInstanceOf[Api].endPoints.head.operations.head.responses.head.customDomainProperties.head

    assertPaginationExtension(extension)
  }

  private def assertPaginationExtension(extension: DomainExtension): Assertion = {
    val extensionValue = extension.fields.getValueAsOption("http://a.ml/vocab#pagination").get

    extension.name.value() shouldBe "pagination"
    extension.definedBy.name.value() shouldBe "pagination"

    assertAnnotations(extensionValue)

    extension.graph.containsProperty("http://a.ml/vocab#pagination") shouldBe true
    extension.graph
      .getObjectByProperty("http://a.ml/vocab#pagination")
      .head
      .graph
      .scalarByProperty("http://a.ml/vocab#PageSize")
      .head shouldBe 5
  }

  private def assertAnnotations(value: Value): Unit = {
    value.annotations.nonEmpty shouldBe true
    value.annotations.find(classOf[LexicalInformation]) shouldNot be(empty)
  }

  private def assertModel(dialect: String, api: String)(assertion: Document => Assertion): Future[Assertion] = {
    val config = APIConfiguration
      .API()
      .withErrorHandlerProvider(() => UnhandledErrorHandler)
      .withDialect(basePath + dialect)
    for {
      nextConfig <- config
      instance   <- nextConfig.baseUnitClient().parseDocument(basePath + api)
    } yield {
      assertion(instance.document)
    }
  }
}