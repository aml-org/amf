package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.Parameter

class MandatoryPathParameters() extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = {
    try {
      model.iterator().foreach {
        case param: Parameter if param.isPath =>
          param.withRequired(true)
        case _ =>
      }
      model
    } catch {
      case _: Exception => model
    }
  }
}
