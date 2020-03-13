package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{pos, sourceOr, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.DomainExtension
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.OrphanAnnotationsEmitter
import amf.plugins.document.webapi.parser.spec.domain.OasResponseEmitter
import amf.plugins.domain.webapi.models.Response
import org.yaml.model.YDocument.EntryBuilder

class ResponsesEmitter(key: String,
                       f: FieldEntry,
                       ordering: SpecOrdering,
                       references: Seq[BaseUnit],
                       orphanAnnotations: Seq[DomainExtension])(implicit val spec: OasSpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val emitters = responses(f, ordering) ++ responsesElementsAnnotations()
    sourceOr(
      f.value.annotations,
      b.entry(
        key,
        _.obj(traverse(emitters, _))
      )
    )
  }

  private def responses(f: FieldEntry, ordering: SpecOrdering): Seq[EntryEmitter] = {
    ordering.sorted(f.array.values.map(e => OasResponseEmitter(e.asInstanceOf[Response], ordering, references)))
  }

  private def responsesElementsAnnotations(): Seq[EntryEmitter] = {
    OrphanAnnotationsEmitter(orphanAnnotations, ordering).emitters
  }

  override def position(): Position = pos(f.value.annotations)
}
