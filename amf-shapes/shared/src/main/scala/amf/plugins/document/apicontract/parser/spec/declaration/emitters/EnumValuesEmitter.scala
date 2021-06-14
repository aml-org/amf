package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.{AmfArray, DataNode}
import amf.core.internal.parser.domain.Value
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.DataNodeEmitter
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
