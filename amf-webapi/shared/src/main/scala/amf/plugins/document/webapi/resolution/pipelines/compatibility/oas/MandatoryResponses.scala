package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.{Operation, Response}

class MandatoryResponses()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
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
