package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.RecursiveShape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder

case class RamlRecursiveShapeTypeEmitter(shape: RecursiveShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    RamlRecursiveShapeEmitter(shape, ordering, references).emitters().foreach(_.emit(b))
  }

  override def position(): Position = pos(shape.annotations)
}
