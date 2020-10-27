package amf.plugins.domain.webapi.resolution.stages

import amf.core.errorhandling.ErrorHandler
import amf.core.model.document.{BaseUnit, Document}
import amf.core.model.domain.AmfElement
import amf.core.parser.FieldEntry
import amf.core.resolution.stages.ResolutionStage
import amf.plugins.domain.shapes.resolution.stages.merge.{AsyncKeyCriteria, CustomMerge, JsonMergePatch}
import amf.plugins.domain.webapi.metamodel.{AbstractModel, MessageModel, OperationModel}
import amf.plugins.domain.webapi.models.{Message, Operation, WebApi}

class JsonMergePatchStage(override implicit val errorHandler: ErrorHandler) extends ResolutionStage() {

  private lazy val merger = JsonMergePatch(_ => false,
                                           AsyncKeyCriteria(),
                                           Seq(OperationModel.Name, MessageModel.Name, AbstractModel.IsAbstract),
                                           Seq(CustomMessageExamplesMerge))

  override def resolve[T <: BaseUnit](model: T): T = model match {
    case doc: Document if doc.encodes.isInstanceOf[WebApi] =>
      val webApi = doc.encodes.asInstanceOf[WebApi]
      resolve(webApi)
      doc.asInstanceOf[T]
    case _ => model
  }

  private def resolve(webApi: WebApi): Unit = {
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
      case (operation, traits) => merge(operation, traits)
    }

  private def mergeMessages(message: Seq[Message]) = message.map(m => (m, getMessageTraits(m))).foreach {
    case (message, traits) => merge(message, traits)
  }

  private def getMessages(operation: Operation): Seq[Message] = operation.requests ++ operation.responses

  private def getOperationTraits(extended: Operation): Seq[Operation] = extended.extend.collect {
    case t: Operation => t
  }
  private def getMessageTraits(extended: Message): Seq[Message] = extended.extend.collect { case t: Message => t }
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
    case _ =>
  }
  val exampleFields = Seq(MessageModel.HeaderExamples, MessageModel.Examples)

  private def definesExamples(patch: Message) = {
    patch.fields.fields().map(_.field).toList.intersect(exampleFields).nonEmpty
  }
}
