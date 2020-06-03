package amf.plugins.domain.webapi.resolution.stages.async

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.webapi.models.{Message, WebApi}
import amf.plugins.domain.webapi.resolution.stages.common.ExamplePropagationHelper

class AsyncExamplePropagationResolutionStage()(override implicit val errorHandler: ErrorHandler)
    extends ResolutionStage
    with ExamplePropagationHelper {

  override def resolve[T <: BaseUnit](model: T): T = model match {
    case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
      propagateExamples(doc.encodes.asInstanceOf[WebApi])
      doc.asInstanceOf[T]
    case _ => model.asInstanceOf[T]
  }

  private def propagateExamples(webApi: WebApi) = {
    val messages = getAllMessages(webApi)
    messages.foreach(propagateExamplesToPayloads)
  }

  private def getAllMessages(webApi: WebApi) =
    webApi.endPoints.flatMap(_.operations).foldLeft(List[Message]()) { (acc, curr) =>
      acc ++ curr.requests ++ curr.responses
    }

  private def propagateExamplesToPayloads(message: Message) =
    message.payloads.map(_.schema).foreach(trackExamplesOf(message, _))

}
