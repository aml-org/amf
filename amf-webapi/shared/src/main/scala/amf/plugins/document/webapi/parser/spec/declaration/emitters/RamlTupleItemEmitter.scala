package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters
import org.yaml.model.YDocument.PartBuilder

case class RamlTupleItemEmitter(item: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends PartEmitter {
  override def emit(b: PartBuilder): Unit = {
    emitters.Raml10TypeEmitter(item, ordering, references = references).entries().foreach { e =>
      b.obj(eb => e.emit(eb))
    }
  }

  override def position(): Position = pos(item.annotations)
}
