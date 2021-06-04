package amf.plugins.document.apicontract.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.apicontract.models.Parameter

class MandatoryPathParameters() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = {
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
