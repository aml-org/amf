package amf.plugins.document.apicontract.parser.spec.declaration.emitters.oas

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.OasLikeShapeEmitterContext
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
