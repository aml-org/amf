package amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations

import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext

case class OrphanAnnotationsEmitter(orphans: Seq[DomainExtension], ordering: SpecOrdering)(
    implicit spec: ShapeEmitterContext) {
  def emitters: Seq[EntryEmitter] = orphans.map(spec.annotationEmitter(_, ordering))
}
