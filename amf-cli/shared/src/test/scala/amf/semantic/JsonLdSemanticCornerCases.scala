package amf.semantic

import amf.apicontract.client.scala.RAMLConfiguration
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.model.domain.{EndPoint, Operation}
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{AmfScalar, DomainElement}
import amf.core.client.scala.vocabulary.ValueType
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.Type._
import amf.core.internal.remote.Mimes
import amf.io.FileAssertionTest
import org.scalatest.{AsyncFunSuite, Matchers}

import scala.concurrent.{ExecutionContext, Future}

class JsonLdSemanticCornerCases extends AsyncFunSuite with Matchers with FileAssertionTest {

  override implicit def executionContext: ExecutionContext = ExecutionContext.Implicits.global
  private val basePath                                     = "file://amf-cli/shared/src/test/resources/semantic/corner"

  test("Semantic extension in wrong domain shouldn't be rendered") {
    val config = RAMLConfiguration.RAML10()
    for {
      loadedConfig <- config.withDialect(s"$basePath/dialect.yaml")
      client       <- Future.successful(loadedConfig.baseUnitClient())
      parseResult  <- client.parseDocument(s"$basePath/api.raml")
      transformed  <- Future.successful(client.transform(parseResult.baseUnit).baseUnit)
    } yield {
      mutateModel(transformed)
      val rendered = client.render(transformed, Mimes.`application/ld+json`)
      val regex    = """"http://a.ml/vocab#operationId": "someOperationId"""".r
      regex.findAllIn(rendered).size shouldBe 1
    }
  }

  test("Semantic extension in wrong domain shouldn't be parsed") {
    val config = RAMLConfiguration.RAML10()
    for {
      loadedConfig <- config.withDialect(s"$basePath/dialect.yaml")
      client       <- Future.successful(loadedConfig.baseUnitClient())
      parseResult  <- client.parseDocument(s"$basePath/operationId-shouldnt-be-parsed.initial.jsonld")
    } yield {
      val unit     = parseResult.document
      val endPoint = unit.encodes.asInstanceOf[WebApi].endPoints.head
      endPoint.graph.containsProperty("http://a.ml/vocab#operationId") shouldBe false
      endPoint.operations.head.graph.containsProperty("http://a.ml/vocab#operationId") shouldBe true
    }
  }

  private def mutateModel(transformed: BaseUnit) = {
    transformed.iterator().foreach {
      case operation: Operation => addOperationIdAnnotation(operation, "some-id")
      case endpoint: EndPoint   => addOperationIdAnnotation(endpoint, "another-id")
      case _                    => // ignore
    }
  }

  private def addOperationIdAnnotation(element: DomainElement, id: String): Unit = {
    element.fields.set(id, Field(Str, ValueType("http://a.ml/vocab#operationId")), AmfScalar("someOperationId"))
  }
}
