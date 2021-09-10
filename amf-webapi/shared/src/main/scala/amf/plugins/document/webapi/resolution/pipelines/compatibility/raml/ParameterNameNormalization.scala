package amf.plugins.document.webapi.resolution.pipelines.compatibility.raml

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.ParameterModel
import amf.plugins.domain.webapi.models.Parameter

class ParameterNameNormalization()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T = {
    try {
      model.iterator().foreach {
        case param: Parameter if param.parameterName.value().nonEmpty =>
          param.set(ParameterModel.Name, param.parameterName.value())
        case _ =>
      }
      model
    } catch {
      case _: Exception => model
    }
  }
}
