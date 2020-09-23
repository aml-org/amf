package amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.extensions.DomainExtension
import amf.plugins.document.webapi.contexts.SpecEmitterContext

case class OrphanAnnotationsEmitter(orphans: Seq[DomainExtension], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext) {
  def emitters: Seq[EntryEmitter] = orphans.map(spec.factory.annotationEmitter(_, ordering))
}
