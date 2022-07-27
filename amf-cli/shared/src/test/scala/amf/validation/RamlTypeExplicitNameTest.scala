package amf.validation
import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.client.scala.model.domain._
import amf.apicontract.client.scala.model.domain.api.{Api, WebApi}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.Shape
import amf.shapes.client.scala.model.domain.NodeShape
import amf.testing.ConfigProvider.configFor
import org.scalatest.Assertion
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers
import scala.concurrent.{ExecutionContext, Future}

class RamlTypeExplicitNameTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val basePath = "file://amf-cli/shared/src/test/resources/validations/raml/type-explicit-name"
  def modelAssertion(path: String)(
      assertion: BaseUnit => Assertion
  ): Future[Assertion] = {
    val parser = APIConfiguration.API().baseUnitClient()
    parser.parse(path) flatMap { parseResult =>
      val specificClient  = configFor(parseResult.sourceSpec).baseUnitClient()
      val transformResult = specificClient.transform(parseResult.baseUnit, PipelineId.Editing)
      assertion(transformResult.baseUnit)
    }
  }

  def getApi(bu: BaseUnit): Api                      = bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi]
  def getFirstEndpoint(bu: BaseUnit): EndPoint       = getApi(bu).endPoints.head
  def getFirstOperation(bu: BaseUnit): Operation     = getFirstEndpoint(bu).operations.head
  def getFirstResponse(bu: BaseUnit): Response       = getFirstOperation(bu).responses.head
  def getFirstRequest(bu: BaseUnit): Request         = getFirstOperation(bu).requests.head
  def getFirstResponsePayload(bu: BaseUnit): Payload = getFirstResponse(bu).payloads.head
  def getFirstPayloadSchema(bu: BaseUnit): Shape     = getFirstResponsePayload(bu).schema

  test("Declared type") {
    val api = s"$basePath/declared-type.raml"
    modelAssertion(api) { bu =>
      val declaredType = bu.asInstanceOf[Document].declares.head.asInstanceOf[Shape]
      declaredType.hasExplicitName shouldBe true
    }
  }

  test("Extended type in header") {
    val api = s"$basePath/extended-type-in-header.raml"
    modelAssertion(api) { bu =>
      val header = getFirstResponse(bu).headers.head
      header.schema.hasExplicitName shouldBe false
    }
  }

  test("Type in header") {
    val api = s"$basePath/type-in-header.raml"
    modelAssertion(api) { bu =>
      val header = getFirstResponse(bu).headers.head
      header.schema.hasExplicitName shouldBe true
    }
  }

  test("Extended type in uriParameters") {
    val api = s"$basePath/extended-type-in-uri-parameters.raml"
    modelAssertion(api) { bu =>
      val uriParam = getFirstEndpoint(bu).parameters.head
      uriParam.schema.hasExplicitName shouldBe false
    }
  }

  test("Type in uriParameters") {
    val api = s"$basePath/type-in-uri-parameters.raml"
    modelAssertion(api) { bu =>
      val uriParam = getFirstEndpoint(bu).parameters.head
      uriParam.schema.hasExplicitName shouldBe true
    }
  }

  test("Extended type in queryString") {
    val api = s"$basePath/extended-type-in-query-string.raml"
    modelAssertion(api) { bu =>
      val queryString = getFirstOperation(bu).request.queryString
      queryString.hasExplicitName shouldBe false
    }
  }

  test("Type in queryString") {
    val api = s"$basePath/type-in-query-string.raml"
    modelAssertion(api) { bu =>
      val queryString = getFirstOperation(bu).request.queryString
      queryString.hasExplicitName shouldBe true
    }
  }

  test("Extended type in queryParameters") {
    val api = s"$basePath/extended-type-in-query-parameter.raml"
    modelAssertion(api) { bu =>
      val queryParam = getFirstRequest(bu).queryParameters.head
      queryParam.schema.hasExplicitName shouldBe false
    }
  }

  test("Type in queryParameters") {
    val api = s"$basePath/type-in-query-parameter.raml"
    modelAssertion(api) { bu =>
      val queryParam = getFirstRequest(bu).queryParameters.head
      queryParam.schema.hasExplicitName shouldBe true
    }
  }

  test("Extended type in body") {
    val api = s"$basePath/extended-type-in-body.raml"
    modelAssertion(api) { bu =>
      val schema = getFirstPayloadSchema(bu)
      schema.hasExplicitName shouldBe false
    }
  }

  test("Type in body") {
    val api = s"$basePath/type-in-body.raml"
    modelAssertion(api) { bu =>
      val schema = getFirstPayloadSchema(bu)
      schema.hasExplicitName shouldBe true
    }
  }

  test("Extended type in properties") {
    val api = s"$basePath/extended-type-in-properties.raml"
    modelAssertion(api) { bu =>
      val property = getFirstPayloadSchema(bu).asInstanceOf[NodeShape].properties.head.range
      property.hasExplicitName shouldBe false
    }
  }

  test("Type in properties") {
    val api = s"$basePath/type-in-properties.raml"
    modelAssertion(api) { bu =>
      val property = getFirstPayloadSchema(bu).asInstanceOf[NodeShape].properties.head.range
      property.hasExplicitName shouldBe true
    }
  }

  test("Built-in type in body") {
    val api = s"$basePath/built-in-type-in-body.raml"
    modelAssertion(api) { bu =>
      val builtInType = getFirstPayloadSchema(bu)
      builtInType.hasExplicitName shouldBe false
    }
  }

  test("Inline type") {
    val api = s"$basePath/inline-type.raml"
    modelAssertion(api) { bu =>
      val inlineType = getFirstPayloadSchema(bu)
      inlineType.hasExplicitName shouldBe false
    }
  }
}
