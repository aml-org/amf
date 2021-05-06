package amf.plugins.domain.webapi.resolution.stages.async

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.TransformationStep
import amf.plugins.domain.webapi.metamodel.MessageModel
import amf.plugins.domain.webapi.models.Message
import amf.plugins.domain.webapi.models.api.Api
import amf.plugins.domain.webapi.resolution.stages.common.ExamplePropagationHelper

class AsyncExamplePropagationResolutionStage() extends TransformationStep with ExamplePropagationHelper {

  override def transform[T <: BaseUnit](model: T, errorHandler: ErrorHandler): T = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      propagateExamples(doc.encodes.asInstanceOf[Api])
      doc.asInstanceOf[T]
    case _ => model.asInstanceOf[T]
  }

  private def propagateExamples(api: Api): Unit = {
    val messages = getAllMessages(api)
    messages.foreach(propagateExamplesToPayloads)
    messages.foreach(propagateHeaderExamplesToParameters)
  }

  private def getAllMessages(api: Api) =
    api.endPoints.flatMap(_.operations).foldLeft(List[Message]()) { (acc, curr) =>
      acc ++ curr.requests ++ curr.responses
    }

  private def propagateExamplesToPayloads(message: Message): Unit =
    message.payloads.map(_.schema).foreach(trackExamplesOf(message, _))

  private def propagateHeaderExamplesToParameters(message: Message): Unit =
    Option(message.headerSchema).foreach(trackExamplesOf(message, _, MessageModel.HeaderExamples))
}
