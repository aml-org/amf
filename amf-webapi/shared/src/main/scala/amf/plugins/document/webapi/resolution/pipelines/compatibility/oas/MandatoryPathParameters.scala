package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.Parameter

class MandatoryPathParameters() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: ErrorHandler): BaseUnit = {
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
