package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.client.common.position.Position
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.RamlShapeEmitterContext
import amf.plugins.domain.shapes.models.{AnyShape, UnionShape}
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
