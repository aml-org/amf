package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.Response
import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.oas.emitter.context.OasSpecEmitterContext
import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.DomainExtension
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{pos, sourceOr, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.OrphanAnnotationsEmitter
import org.yaml.model.YDocument.EntryBuilder

class ResponsesEmitter(key: String,
                       f: FieldEntry,
                       ordering: SpecOrdering,
                       references: Seq[BaseUnit],
                       orphanAnnotations: Seq[DomainExtension])(implicit val spec: OasSpecEmitterContext)
    extends EntryEmitter {
  protected implicit val shapeCtx: ShapeEmitterContext = AgnosticShapeEmitterContextAdapter(spec)
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
