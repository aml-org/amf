package amf.plugins.domain.apicontract.resolution.stages.async

import amf.core.errorhandling.AMFErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.apicontract.models.api.Api
import amf.plugins.domain.apicontract.resolution.stages.common.ExamplePropagationHelper

class ServerVariableExampleResolutionStage() extends TransformationStep with ExamplePropagationHelper {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      propagateInServerVariables(doc.encodes.asInstanceOf[Api])
      doc
    case _ => model
  }

  private def propagateInServerVariables(api: Api): Unit = {
    val params = api.servers.flatMap(_.variables)
    params.foreach(param => Option(param.schema).map(trackExamplesOf(param, _)))
  }

}
