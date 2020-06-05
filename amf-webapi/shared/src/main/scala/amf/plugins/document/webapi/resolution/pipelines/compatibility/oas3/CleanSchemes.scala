package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.metamodel.{OperationModel, WebApiModel}
import amf.plugins.domain.webapi.models.{Operation, WebApi}

class CleanSchemes()(override implicit val errorHandler: ErrorHandler) extends ResolutionStage {

  override def resolve[T <: BaseUnit](model: T): T =
    try {
      model
        .iterator()
        .foreach({
          case operation: Operation => operation.fields.removeField(OperationModel.Schemes)
          case webapi: WebApi       => webapi.fields.removeField(WebApiModel.Schemes)
          case _                    => // ignore
        })
      model
    } catch {
      case _: Throwable => model
    }
}
