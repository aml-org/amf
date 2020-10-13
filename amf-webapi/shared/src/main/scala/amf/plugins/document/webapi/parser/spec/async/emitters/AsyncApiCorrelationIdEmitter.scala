package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.domain.webapi.metamodel.CorrelationIdModel
import amf.plugins.domain.webapi.models.CorrelationId
import org.yaml.model.YDocument
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}
import amf.core.emitter.BaseEmitters._
import amf.plugins.document.webapi.parser.spec.OasDefinitions
import amf.plugins.document.webapi.parser.spec.declaration.OasTagToReferenceEmitter

import scala.collection.mutable.ListBuffer

class AsyncApiCorrelationIdEmitter(correlationId: CorrelationId, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = correlationId.fields
    b.entry(
      "correlationId",
      AsyncApiCorrelationIdContentEmitter(correlationId, ordering).emit(_)
    )
  }

  override def position(): Position = pos(correlationId.annotations)
}

case class AsyncApiCorrelationIdContentEmitter(idObj: CorrelationId, ordering: SpecOrdering)(
    implicit val spec: OasLikeSpecEmitterContext)
    extends PartEmitter {

  override def emit(b: YDocument.PartBuilder): Unit = {
    val fs = idObj.fields
    sourceOr(
      idObj.annotations,
      if (idObj.isLink)
        emitLink(b)
      else {
        b.obj { emitter =>
          {
            val result = ListBuffer[EntryEmitter]()
            fs.entry(CorrelationIdModel.Description).map(f => result += ValueEmitter("description", f))
            fs.entry(CorrelationIdModel.Location).map(f => result += ValueEmitter("location", f))
            traverse(ordering.sorted(result), emitter)
          }
        }
      }
    )
  }

  private def emitLink(b: PartBuilder): Unit = OasTagToReferenceEmitter(idObj).emit(b)

  override def position(): Position = pos(idObj.annotations)
}

case class AsyncCorrelationIdDeclarationsEmitter(ids: Seq[CorrelationId], ordering: SpecOrdering)(
    implicit spec: OasLikeSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    b.entry(
      "correlationIds",
      _.obj(entryBuilder => {
        ids.foreach(idObj => {
          val emitter = AsyncApiCorrelationIdContentEmitter(idObj, ordering)
          entryBuilder.entry(idObj.name.value(), b => emitter.emit(b))
        })
      })
    )
  }

  override def position(): Position = ids.headOption.map(p => pos(p.annotations)).getOrElse(ZERO)
}
