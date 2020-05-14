package amf.plugins.document.webapi.contexts.emitter.oas

import amf.core.emitter.{Emitter, EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.document.webapi.parser.spec.declaration.CompactOasTypesEmitters
import amf.plugins.document.webapi.parser.spec.declaration.emitters.schema.json.{
  CompactOasRecursiveShapeEmitter,
  CompactOasTypeEmitter
}

trait OasCompactEmitterFactory {
  implicit val spec: OasSpecEmitterContext

  def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    CompactOasTypesEmitters.apply

  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field],
                   references: Seq[BaseUnit],
                   pointer: Seq[String],
                   schemaPath: Seq[(String, String)]): Seq[Emitter] =
    CompactOasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()

  def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter =
    (shape, ordering, list) => new CompactOasRecursiveShapeEmitter(shape, ordering, list)

}
