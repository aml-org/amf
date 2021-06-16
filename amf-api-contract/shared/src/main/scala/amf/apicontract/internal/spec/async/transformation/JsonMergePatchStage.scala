package amf.apicontract.internal.spec.async.transformation

import amf.apicontract.client.scala.model.domain.api.Api
import amf.apicontract.client.scala.model.domain.{Message, Operation, Payload}
import amf.apicontract.internal.metamodel.domain.MessageModel
import amf.apicontract.internal.metamodel.domain.PayloadModel.{MediaType, SchemaMediaType}
import amf.apicontract.internal.spec.common.transformation.stage.CustomMerge
import amf.core.client.scala.errorhandling.AMFErrorHandler
import amf.core.client.scala.model.document.{BaseUnit, Document}
import amf.core.client.scala.model.domain.{AmfElement, AmfObject, DomainElement}
import amf.core.client.scala.transform.stages.TransformationStep
import amf.core.internal.metamodel.Field
import amf.core.internal.metamodel.domain.DomainElementModel

class JsonMergePatchStage(isEditing: Boolean) extends TransformationStep() {

  private lazy val merger = AsyncJsonMergePatch()

  override def transform(model: BaseUnit, errorHandler: AMFErrorHandler): BaseUnit = model match {
    case doc: Document if doc.encodes.isInstanceOf[Api] =>
      val webApi = doc.encodes.asInstanceOf[Api]
      resolve(webApi)
      doc
    case _ => model
  }

  private def resolve(webApi: Api): Unit = {
    val operations = webApi.endPoints.flatMap(_.operations)
    mergeOperations(operations)
    val messages = operations.flatMap(getMessages)
    mergeMessages(messages)
  }

  private def merge(target: AmfElement, patches: Seq[AmfElement]) = patches.foldLeft(target) { (acc, curr) =>
    merger.merge(acc, curr)
  }

  private def mergeOperations(operations: Seq[Operation]) =
    operations.map(o => (o, getOperationTraits(o))).foreach {
      case (operation, traits) =>
        merge(operation, traits)
        if (!isEditing) removeExtends(operation)
    }

  private def mergeMessages(message: Seq[Message]) = message.map(m => (m, getMessageTraits(m))).foreach {
    case (message, traits) =>
      merge(message, traits)
      if (!isEditing) removeExtends(message)
  }

  private def getMessages(operation: Operation): Seq[Message] = operation.requests ++ operation.responses

  private def getOperationTraits(extended: Operation): Seq[Operation] = extended.extend.collect {
    case t: Operation => t
  }
  private def getMessageTraits(extended: Message): Seq[Message] = extended.extend.collect { case t: Message => t }

  private def removeExtends(element: DomainElement) = element.fields.removeField(DomainElementModel.Extends)
}

object CustomMessageExamplesMerge extends CustomMerge {
  override def apply(target: AmfElement, patch: AmfElement): Unit = (target, patch) match {
    case (target: Message, patch: Message) =>
      if (definesExamples(patch)) {
        exampleFields.foreach(target.fields.removeField)
        exampleFields.foreach { exampleField =>
          patch.fields
            .getValueAsOption(exampleField)
            .foreach { value =>
              target.set(exampleField, value.value)
            }
        }
      }
    case _ => // ignore
  }
  val exampleFields = Seq(MessageModel.HeaderExamples, MessageModel.Examples)

  private def definesExamples(patch: Message) = {
    patch.fields.fields().map(_.field).toList.intersect(exampleFields).nonEmpty
  }
}

object PayloadMediaTypeMerge extends CustomMerge {
  override def apply(target: AmfElement, patch: AmfElement): Unit = (target, patch) match {
    case (target: Message, patch: Message) =>
      patch.payloads.headOption.foreach { payload =>
        val targetPayload = target.payloads.headOption.fold(Payload()) { targetPayload =>
          payloadFieldsToCopy.map(f => copyField(targetPayload, payload, f))
          targetPayload
        }
        target.withPayloads(Seq(targetPayload))
      }
    case _ => // ignore
  }

  private def payloadFieldsToCopy = Set(MediaType, SchemaMediaType)

  private def copyField(target: AmfObject, patch: AmfObject, field: Field): AmfObject = {
    patch.fields.getValueAsOption(field).foreach { value =>
      target.set(field, value.toString)
    }
    target
  }
}
