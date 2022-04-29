package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument.PartBuilder

case class RamlTupleItemEmitter(item: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: RamlShapeEmitterContext
) extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    Raml10TypeEmitter(item, ordering, references = references).entries().foreach { e =>
      b.obj(eb => e.emit(eb))
    }
  }

  override def position(): Position = pos(item.annotations)
}
