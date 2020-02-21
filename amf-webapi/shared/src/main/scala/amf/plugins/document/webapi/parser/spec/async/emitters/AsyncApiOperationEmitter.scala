package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.AmfObject
import amf.core.parser.{FieldEntry, Fields, Position}
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.contexts.emitter.async.AsyncSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.async.emitters.bindings.AsyncApiOperationBindingsEmitter
import amf.plugins.document.webapi.parser.spec.async.{AsyncHelper, Publish, Subscribe}
import amf.plugins.document.webapi.parser.spec.oas.emitters.{
  OasLikeOperationEmitter,
  StringArrayTagsEmitter,
  TagsEmitter
}
import amf.plugins.domain.webapi.annotations.OrphanOasExtension
import amf.plugins.domain.webapi.metamodel.OperationModel
import amf.plugins.domain.webapi.models.bindings.OperationBinding
import amf.plugins.domain.webapi.models.{Message, Operation, Tag}
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

class AsyncApiOperationEmitter(operation: Operation, ordering: SpecOrdering)(implicit spec: OasLikeSpecEmitterContext)
    extends OasLikeOperationEmitter(operation, ordering) {

  override def emitSpecific(fs: Fields, tempResult: ListBuffer[EntryEmitter]): ListBuffer[EntryEmitter] = {
    val bindingOrphanAnnotations =
      operation.customDomainProperties.filter(_.extension.annotations.contains(classOf[OrphanOasExtension]))
    fs.entry(OperationModel.Bindings)
      .map(f => tempResult += new AsyncApiBindingsEmitter(f, ordering, bindingOrphanAnnotations))
    fs.entry(OperationModel.Tags)
      .map(f => tempResult += TagsEmitter("tags", f.array.values.asInstanceOf[Seq[Tag]], ordering))
    emitMessage(fs).map(reqOrRes => tempResult += reqOrRes)
    tempResult
  }

  def emitMessage(fs: Fields): Option[EntryEmitter] = {
    AsyncHelper.messageType(operation.method.value()) match {
      case Some(s) => fs.entry(s.field).map(new AsyncApiMessageEmitter(_, ordering))
      case _       => None
    }
  }
}
