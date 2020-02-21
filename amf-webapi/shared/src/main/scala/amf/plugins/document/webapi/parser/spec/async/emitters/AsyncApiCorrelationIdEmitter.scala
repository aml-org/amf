package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.emitter.BaseEmitters.{ValueEmitter, pos, traverse}
import amf.core.parser.Position
import amf.plugins.domain.webapi.metamodel.CorrelationIdModel
import amf.plugins.domain.webapi.models.CorrelationId
import org.yaml.model.YDocument

import scala.collection.mutable.ListBuffer

class AsyncApiCorrelationIdEmitter(correlationId: CorrelationId, ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit = {
    val result = ListBuffer[EntryEmitter]()
    val fs     = correlationId.fields
    b.entry(
      "correlationId",
      _.obj { emitter =>
        fs.entry(CorrelationIdModel.Description).map(f => result += ValueEmitter("description", f))
        fs.entry(CorrelationIdModel.Location).map(f => result += ValueEmitter("location", f))
        traverse(ordering.sorted(result), emitter)
      }
    )
  }

  override def position(): Position = pos(correlationId.annotations)
}
