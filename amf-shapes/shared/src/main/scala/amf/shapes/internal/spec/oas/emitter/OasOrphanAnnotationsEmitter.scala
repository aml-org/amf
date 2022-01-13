package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.OasAstAnnotationEmitter

case class OasOrphanAnnotationsEmitter(orphans: Seq[DomainExtension], ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext) {
  def emitters: Seq[EntryEmitter] = orphans.map(OasAstAnnotationEmitter(_, ordering))
}
