package amf.plugins.document.webapi.parser.spec.oas

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.parser.spec.declaration.emitters.OasLikeShapeEmitterContext

object OasDeclaredShapesEmitter {
  def apply(shapes: Seq[Shape], ordering: SpecOrdering, references: Seq[BaseUnit] = Seq())(
      implicit spec: OasLikeShapeEmitterContext): Option[EntryEmitter] = {
    if (shapes.nonEmpty || spec.definitionsQueue.nonEmpty())
      Some(spec.declaredTypesEmitter(shapes, references, ordering))
    else None
  }
}
