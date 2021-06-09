package amf.plugins.document.apicontract.contexts.emitter.oas

import amf.core.emitter.{Emitter, EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.{RecursiveShape, Shape}
import amf.plugins.document.apicontract.parser.spec.declaration.{CompactOasTypesEmitters, OasDeclaredTypesEmitters}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas.{OasRecursiveShapeEmitter, OasTypeEmitter}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.schema.json.{
  CompactOasRecursiveShapeEmitter,
  CompactOasTypeEmitter
}

trait OasCompactEmitterFactory {

  protected implicit val shapeCtx: OasLikeShapeEmitterContext
  lazy val compactEmissionEnabled: Boolean = shapeCtx.options.compactedEmission

  def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    if (compactEmissionEnabled)
      CompactOasTypesEmitters.apply
    else
      OasDeclaredTypesEmitters.apply

  def typeEmitters(shape: Shape,
                   ordering: SpecOrdering,
                   ignored: Seq[Field],
                   references: Seq[BaseUnit],
                   pointer: Seq[String],
                   schemaPath: Seq[(String, String)]): Seq[Emitter] = {
    if (compactEmissionEnabled)
      CompactOasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
    else
      OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
  }

  def recursiveShapeEmitter(shape: RecursiveShape,
                            ordering: SpecOrdering,
                            schemaPath: Seq[(String, String)]): EntryEmitter =
    if (compactEmissionEnabled)
      new CompactOasRecursiveShapeEmitter(shape, ordering, schemaPath)
    else
      OasRecursiveShapeEmitter(shape, ordering, schemaPath)

}
