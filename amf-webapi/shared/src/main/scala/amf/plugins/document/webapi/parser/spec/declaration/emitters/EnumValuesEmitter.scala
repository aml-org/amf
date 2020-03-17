package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.{AmfArray, DataNode}
import amf.core.parser.{Position, Value}
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.DataNodeEmitter
import org.yaml.model.YDocument.EntryBuilder

case class EnumValuesEmitter(key: String, value: Value, ordering: SpecOrdering)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val nodes = value.value.asInstanceOf[AmfArray].values.asInstanceOf[Seq[DataNode]]
    val emitters = nodes.map { d =>
      DataNodeEmitter(d, ordering)(spec.eh)
    }
    b.entry(key, _.list(traverse(ordering.sorted(emitters), _)))
  }

  override def position(): Position = pos(value.annotations)
}
