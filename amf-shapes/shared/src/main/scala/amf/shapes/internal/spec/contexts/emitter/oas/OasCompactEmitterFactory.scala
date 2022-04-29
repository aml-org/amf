package amf.shapes.internal.spec.contexts.emitter.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.{RecursiveShape, Shape}
import amf.core.internal.metamodel.Field
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter}
import amf.shapes.internal.spec.common.emitter.{
  CompactOasTypesEmitters,
  OasDeclaredTypesEmitters,
  OasLikeShapeEmitterContext
}
import amf.shapes.internal.spec.oas.emitter.compact.{CompactOasRecursiveShapeEmitter, CompactOasTypeEmitter}
import amf.shapes.internal.spec.oas.emitter.{OasRecursiveShapeEmitter, OasTypeEmitter}

trait OasCompactEmitterFactory {

  protected implicit val shapeCtx: OasLikeShapeEmitterContext
  lazy val compactEmissionEnabled: Boolean = shapeCtx.options.compactedEmission

  def declaredTypesEmitter: (Seq[Shape], Seq[BaseUnit], SpecOrdering) => EntryEmitter =
    if (compactEmissionEnabled)
      CompactOasTypesEmitters.apply
    else
      OasDeclaredTypesEmitters.apply

  def typeEmitters(
      shape: Shape,
      ordering: SpecOrdering,
      ignored: Seq[Field],
      references: Seq[BaseUnit],
      pointer: Seq[String],
      schemaPath: Seq[(String, String)]
  ): Seq[Emitter] = {
    if (compactEmissionEnabled)
      CompactOasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
    else
      OasTypeEmitter(shape, ordering, ignored, references, pointer, schemaPath).emitters()
  }

  def recursiveShapeEmitter(
      shape: RecursiveShape,
      ordering: SpecOrdering,
      schemaPath: Seq[(String, String)]
  ): EntryEmitter =
    if (compactEmissionEnabled)
      new CompactOasRecursiveShapeEmitter(shape, ordering, schemaPath)
    else
      OasRecursiveShapeEmitter(shape, ordering, schemaPath)

}
