package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{
  OasLikeShapeEmitterContext,
  ShapeEmitterContext
}
import amf.plugins.domain.shapes.models.UnionShape

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
