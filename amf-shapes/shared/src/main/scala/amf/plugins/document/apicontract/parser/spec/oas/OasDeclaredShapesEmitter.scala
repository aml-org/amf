package amf.plugins.document.apicontract.parser.spec.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext

object OasDeclaredShapesEmitter {
  def apply(shapes: Seq[Shape], ordering: SpecOrdering, references: Seq[BaseUnit] = Seq())(
      implicit spec: OasLikeShapeEmitterContext): Option[EntryEmitter] = {
    if (shapes.nonEmpty || spec.definitionsQueue.nonEmpty())
      Some(spec.declaredTypesEmitter(shapes, references, ordering))
    else None
  }
}
