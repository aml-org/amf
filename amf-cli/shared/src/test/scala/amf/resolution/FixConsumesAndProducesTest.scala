package amf.resolution

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.client.scala.model.domain.api.WebApi
import amf.apicontract.client.scala.{APIConfiguration, OASConfiguration}
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.StrField
import amf.core.client.scala.model.document.{BaseUnit, Document}
import org.scalatest
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class FixConsumesAndProducesTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global
  val basePath = "file://amf-cli/shared/src/test/resources/resolution/fix-consumes-and-produces/"

  test("accepts should include mediaTypes defined at request body") {
    val api = basePath + "request-with-mediatype.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.accepts) == Seq("multipart/form-data", "application/json"))
    }
  }

  test("contentType should include mediaTypes defined at response body") {
    val api = basePath + "response-with-mediatype.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.contentType) == Seq("application/json"))
    }
  }

  test("mediaType present in two responses should appear once in contentType") {
    val api = basePath + "repeated-mediatypes-in-response.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.contentType) == Seq("text/plain"))
    }
  }


  test("accepts should include only mediaTypes defined at operation level if both levels defined") {
    val api = basePath + "req-with-mediatype-at-both-levels.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.accepts) == Seq("multipart/form-data"))
    }
  }

  test("contentType should include only mediaTypes defined at operation level if both levels defined") {
    val api = basePath + "resp-with-mediatype-at-both-levels.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.contentType) == Seq("text/plain"))
    }
  }

  test("contentType should include all mediaTypes from a response") {
    val api = basePath + "response-with-multiple-mediatypes.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.contentType) == Seq("application/json", "text/plain"))
    }
  }

  test("contentType should include the mediaTypes of all responses") {
    val api = basePath + "multiple-responses-with-mediatypes.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.contentType) == Seq("application/json", "text/plain"))
    }
  }

  test("accepts should be empty if no mediaType is defined") {
    val api = basePath + "no-mediatype.raml"
    assertOperationSatisfy(api) { op =>
      assert(op.accepts.isEmpty)
    }
  }

  test("contentType should be empty if no mediaType defined") {
    val api = basePath + "no-mediatype.raml"
    assertOperationSatisfy(api) { op =>
      assert(op.contentType.isEmpty)
    }
  }

  test("accepts should be equal to global mediaType if it is the only one defined") {
    val api = basePath + "only-global-mediatype.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.accepts) == Seq("application/json"))
    }
  }

  test("contentType should be equal to global mediaType if it is the only one defined") {
    val api = basePath + "only-global-mediatype.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.contentType) == Seq("application/json"))
    }
  }

  test("accepts should include only allowed mediaTypes when request has parameter with type file in body") {
    val api = basePath + "request-with-file-parameter.raml"
    assertOperationSatisfy(api) { op =>
      assert(mediaTypes(op.accepts) == Seq("multipart/form-data", "application/x-www-form-urlencoded"))
    }
  }

  private def mediaTypes(set: Seq[StrField]) = set.map(_.value())

  private def assertOperationSatisfy(api: String)(assertion: Operation => scalatest.Assertion) = {
    parseAndResolve(api).map { bu =>
      val operation = bu.asInstanceOf[Document].encodes.asInstanceOf[WebApi].endPoints.head.operations.head
      assertion(operation)
    }
  }

  private def parseAndResolve(api: String): Future[BaseUnit] = {
    APIConfiguration.API().baseUnitClient().parse(api).map { result =>
      val transformConfig = OASConfiguration.OAS20()
      transformConfig.baseUnitClient().transform(result.baseUnit, PipelineId.Compatibility).baseUnit
    }
  }
}
