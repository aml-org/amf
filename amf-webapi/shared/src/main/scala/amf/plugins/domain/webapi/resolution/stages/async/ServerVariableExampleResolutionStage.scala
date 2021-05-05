package amf.plugins.domain.webapi.resolution.stages.async

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.domain.webapi.resolution.stages.common.ExamplePropagationHelper

class ServerVariableExampleResolutionStage() extends TransformationStep with ExamplePropagationHelper {

  override def apply[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      propagateInServerVariables(doc.encodes.asInstanceOf[Api])
      doc.asInstanceOf[T]
    case _ => model.asInstanceOf[T]
  }

  private def propagateInServerVariables(api: Api): Unit = {
    val params = api.servers.flatMap(_.variables)
    params.foreach(param => Option(param.schema).map(trackExamplesOf(param, _)))
  }

}
