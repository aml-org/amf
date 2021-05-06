package amf.plugins.document.webapi.resolution.pipelines.compatibility.oas3

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.BaseUnit
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.metamodel.api.BaseApiModel
import amf.plugins.domain.webapi.models.Operation
import amf.plugins.domain.webapi.models.api.Api

class CleanSchemes() extends TransformationStep {

  override def transform[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T =
    try {
      model
        .iterator()
        .foreach({
          case operation: Operation => operation.fields.removeField(OperationModel.Schemes)
          case api: Api             => api.fields.removeField(BaseApiModel.Schemes)
          case _                    => // ignore
        })
      model
    } catch {
      case _: Throwable => model
    }
}
