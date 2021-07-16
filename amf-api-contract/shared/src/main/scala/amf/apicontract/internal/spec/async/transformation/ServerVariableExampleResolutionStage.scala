package amf.apicontract.internal.spec.async.transformation

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.spec.common.transformation.stage.ExamplePropagationHelper
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.TransformationStep

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
