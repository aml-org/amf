package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext

case class OasUnionShapeEmitter(shape: UnionShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit],
                                pointer: Seq[String] = Nil,
                                schemaPath: Seq[(String, String)] = Nil,
                                isHeader: Boolean = false)(implicit spec: OasLikeShapeEmitterContext)
    extends OasAnyShapeEmitter(shape, ordering, references, isHeader = isHeader) {

  override def emitters(): Seq[EntryEmitter] =
    super.emitters() ++ Seq(OasAnyOfShapeEmitter(shape, ordering, references, pointer, schemaPath))
}
