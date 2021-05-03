package amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.extensions.DomainExtension
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext

case class OrphanAnnotationsEmitter(orphans: Seq[DomainExtension], ordering: SpecOrdering)(
  implicit spec: ShapeEmitterContext) {
  def emitters: Seq[EntryEmitter] = orphans.map(spec.annotationEmitter(_, ordering))
}
