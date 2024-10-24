package amf.shapes.internal.spec.common.emitter

import amf.core.client.scala.model.domain.{AmfArray, DataNode}
import amf.core.internal.datanode.DataNodeEmitter
import amf.core.internal.parser.domain.Value
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.mulesoft.common.client.lexical.Position
import org.yaml.model.YDocument.EntryBuilder

case class EnumValuesEmitter(key: String, value: Value, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
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
