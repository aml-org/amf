package amf.plugins.document.apicontract.resolution.pipelines.compatibility.oas3

import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep
import amf.plugins.domain.apicontract.models.{Operation, Response}

class MandatoryResponses() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
    try {
      model.iterator().foreach {
        case operation: Operation =>
          if (operation.responses.isEmpty) {
            operation.withResponses(Seq(Response().withName("200").withStatusCode("200").withDescription("")))
          }
        case _ =>
      }
      model
    } catch {
      case _: Exception => model
    }
  }

}
