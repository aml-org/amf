package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext

object OasDeclaredShapesEmitter {
  def apply(shapes: Seq[Shape], ordering: SpecOrdering, references: Seq[BaseUnit] = Seq())(implicit
      spec: OasLikeShapeEmitterContext
  ): Option[EntryEmitter] = {
    if (shapes.nonEmpty || spec.definitionsQueue.nonEmpty())
      Some(spec.declaredTypesEmitter(shapes, references, ordering))
    else None
  }
}
