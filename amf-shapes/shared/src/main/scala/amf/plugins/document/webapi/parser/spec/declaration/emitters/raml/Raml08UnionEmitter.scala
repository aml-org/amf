package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.parser.Position
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.domain.shapes.models.{AnyShape, UnionShape}
import org.yaml.model.YDocument.PartBuilder

case class Raml08UnionEmitter(union: UnionShape, ordering: SpecOrdering)(implicit spec: ShapeEmitterContext)
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
