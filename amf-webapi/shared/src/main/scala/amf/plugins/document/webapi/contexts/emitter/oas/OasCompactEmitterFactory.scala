package amf.plugins.document.webapi.contexts.emitter.oas

import amf.core.emitter.{Emitter, EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.document.webapi.parser.spec.declaration.{CompactOasTypesEmitters, OasDeclaredTypesEmitters}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.oas.{OasRecursiveShapeEmitter, OasTypeEmitter}
import amf.plugins.document.webapi.parser.spec.declaration.emitters.schema.json.{
  CompactOasRecursiveShapeEmitter,
  CompactOasTypeEmitter
}

trait OasCompactEmitterFactory {
  implicit val spec: OasSpecEmitterContext

  def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    if (spec.compactEmission)
      CompactOasTypesEmitters.apply
    else
      OasDeclaredTypesEmitters.apply

  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field],
                   references: Seq[BaseUnit],
                   pointer: Seq[String],
                   schemaPath: Seq[(String, String)]): Seq[Emitter] = {
    if (spec.compactEmission)
      CompactOasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
    else
      OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
  }

  def recursiveShapeEmitter: (RecursiveShape, SpecOrdering, Seq[(String, String)]) => EntryEmitter =
    (shape, ordering, list) =>
      if (spec.compactEmission)
        new CompactOasRecursiveShapeEmitter(shape, ordering, list)
      else
        OasRecursiveShapeEmitter(shape, ordering, list)

}
