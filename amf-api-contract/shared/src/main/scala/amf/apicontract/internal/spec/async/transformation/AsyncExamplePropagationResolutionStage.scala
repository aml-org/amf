package amf.apicontract.internal.spec.async.transformation

import amf.apicontract.client.scala.model.domain.Message
import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.internal.metamodel.domain.MessageModel
import amf.apicontract.internal.spec.common.transformation.stage.ExamplePropagationHelper
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.transform.stages.TransformationStep

class AsyncExamplePropagationResolutionStage() extends TransformationStep with ExamplePropagationHelper {

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      propagateExamples(doc.encodes.asInstanceOf[Api])
      doc
    case _ => model
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
