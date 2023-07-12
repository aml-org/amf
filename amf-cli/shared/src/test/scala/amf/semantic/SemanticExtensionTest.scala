package amf.semantic

import amf.aml.client.scala.model.domain.DialectDomainElement
import amf.apicontract.client.scala.model.domain.api.Api
import amf.core.client.scala.model.document.Document
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.internal.parser.domain.Fields
import amf.core.client.scala.model.domain.{AmfElement, AmfScalar, DomainElement}
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

  test("Apply SemEx to Info node on OAS API") {
    val testDir = "info-object"
    assertModel(s"$testDir/dialect.yaml", s"$testDir/api.yaml", Oas30) { doc =>
      val assertion = for {
        technologyNode <- findExtension[DomainElement](
          doc.encodes,
          "http://mycompany.org/extensions/cataloging#technologyObject"
        )
        technologyValue <- technologyNode.fields
          .getValueAsOption("http://mycompany.org/extensions/cataloging#technology")
          .map(_.value.asInstanceOf[AmfScalar].toString)
      } yield {
        technologyValue should be("ASD")
      }
      assertion.getOrElse(fail("Technology extension not found"))
    }
  }

  test("Nested semex > Nesting without semex") {
    val testDir = "nested-semex-1"
    assertModel(s"$testDir/dialect.yaml", s"$testDir/api.yaml", Oas30) { doc =>
      val webAPIExtensions = extensionsFrom(doc.encodes)
      val childName = for {
        parent <- webAPIExtensions.getValueAsOption("http://a.ml/vocab#parent")
        child  <- parent.value.asInstanceOf[DomainElement].fields.getValueAsOption("http://a.ml/vocabularies/data#x-child")
        childName <- child.value.asInstanceOf[DomainElement].fields.getValueAsOption("http://a.ml/vocabularies/data#name")
      } yield {
        childName.value.asInstanceOf[AmfScalar].toString
      }
      assert(childName.contains("Juan"))
    }
  }

  test("Nested semex > Nesting with semex") {
    val testDir = "nested-semex-2"
    assertModel(s"$testDir/dialect.yaml", s"$testDir/api.yaml", Oas30) { doc =>
      val webAPIExtensions = extensionsFrom(doc.encodes)
      val childName = for {
        parent <- webAPIExtensions.getValueAsOption("http://a.ml/vocab#parent")
        child <- extensionsFrom(parent.value.asInstanceOf[DomainElement]).getValueAsOption("http://a.ml/vocab#child")
        childName <- child.value.asInstanceOf[DomainElement].fields.getValueAsOption("http://a.ml/vocabularies/data#name")
      } yield {
        childName.value.asInstanceOf[AmfScalar].toString
      }
      assert(childName.contains("Juan"))
    }
  }

  private def extensionsFrom(d: DomainElement): Fields = d.customDomainProperties.head.fields

  private def lookupOperation(document: Document) = {
    val extension =
      document.encodes.asInstanceOf[Api].endPoints.head.operations.head.customDomainProperties.head

    assertPaginationExtension(extension, 10)
  }

  private def findExtension[T <: AmfElement](d: DomainElement, extension: String) = {
    d.customDomainProperties.head.fields.getValueAsOption(extension).map(_.value.asInstanceOf[T])
  }

  private def lookupEndpoint(document: Document) = {
    val extension =
      document.encodes.asInstanceOf[Api].endPoints.head.customDomainProperties.head

    assertPaginationExtension(extension, 15)
  }
}
