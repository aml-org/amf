package amf.resolution

import amf.apicontract.client.scala.APIConfiguration
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.core.client.common.transform.PipelineId
import amf.core.client.scala.model.document.BaseUnit
import org.scalatest.funsuite.AsyncFunSuite
import org.scalatest.matchers.should.Matchers

import scala.concurrent.{ExecutionContext, Future}

class SourceInformationTest extends AsyncFunSuite with Matchers {
  override implicit val executionContext: ExecutionContext = ExecutionContext.Implicits.global

  test("Correct location of element defined in external trait using source information") {
    val api = "file://amf-cli/shared/src/test/resources/production/lib-trait-location/api.raml"
    parseAndEditing(api).map { unit =>
      val operation   = unit.findByType(OperationModel.`type`.head.iri()).head
      val operationId = operation.id
      val correctLocation = unit.sourceInformation.additionalLocations.exists(
        locInfo =>
          locInfo.locationValue.value().contains("lib-trait-location/lib.raml") &&
            locInfo.elements.exists(_.value() == operationId))
      assert(correctLocation)
    }
  }

  def parseAndEditing(api: String): Future[BaseUnit] = {
    APIConfiguration.API().baseUnitClient().parse(api).map { result =>
      val transformConfig = APIConfiguration.fromSpec(result.sourceSpec)
      transformConfig.baseUnitClient().transform(result.baseUnit, PipelineId.Editing).baseUnit
    }
  }

}
