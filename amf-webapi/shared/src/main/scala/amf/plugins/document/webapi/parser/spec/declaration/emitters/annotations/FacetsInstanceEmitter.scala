package amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.extensions.ShapeExtension
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import org.yaml.model.YDocument.EntryBuilder

abstract class FacetsInstanceEmitter(shapeExtension: ShapeExtension, ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  val name: String

  override def emit(b: EntryBuilder): Unit = {
    b.complexEntry(
      b => {
        b += name
      },
      b => {
        Option(shapeExtension.extension).foreach {
          DataNodeEmitter(_, ordering)(spec.eh).emit(b)
        }
      }
    )
  }

  override def position(): Position = pos(shapeExtension.annotations)
}
