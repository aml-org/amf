package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas

import amf.core.model.document.BaseUnit
import amf.core.parser.ErrorHandler
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter

class MandatoryPathParameters()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model.findByType(ParameterModel.`type`.head.iri()).foreach {
        case param: Parameter if param.isPath =>
          param.withRequired(true)
        case other                                =>
          other
      }
      model
    } catch {
      case _: Exception => model
    }
  }

}
