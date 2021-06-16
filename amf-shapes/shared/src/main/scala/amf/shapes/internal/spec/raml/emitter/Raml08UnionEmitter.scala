package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.client.scala.domain.models.UnionShape
import amf.shapes.client.scala.model.domain.{AnyShape, UnionShape}
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument.PartBuilder

case class Raml08UnionEmitter(union: UnionShape, ordering: SpecOrdering)(implicit spec: RamlShapeEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    b.list(b => {
      union.anyOf
        .collect({ case s: AnyShape => s })
        .foreach(s => {
          Raml08TypePartEmitter(s, ordering, Seq()).emit(b)
        })
    })
  }

  override def position(): Position = pos(union.annotations)
}
