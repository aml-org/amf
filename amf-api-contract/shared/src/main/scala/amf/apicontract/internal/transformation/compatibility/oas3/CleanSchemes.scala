package amf.apicontract.internal.transformation.compatibility.oas3

import amf.apicontract.client.scala.model.domain.Operation
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.OperationModel
import amf.apicontract.internal.metamodel.domain.api.BaseApiModel
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.transform.stages.TransformationStep

class CleanSchemes() extends TransformationStep {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit =
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
