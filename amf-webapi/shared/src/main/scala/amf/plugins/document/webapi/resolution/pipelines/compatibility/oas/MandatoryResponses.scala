package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.{Operation, Response}

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
