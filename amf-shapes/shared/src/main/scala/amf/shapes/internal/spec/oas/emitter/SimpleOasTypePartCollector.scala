package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.Emitter
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext

class SimpleOasTypePartCollector(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field], references: Seq[BaseUnit])(
    implicit spec: OasLikeShapeEmitterContext)
    extends OasTypePartCollector(shape, ordering, ignored, references) {

  def computeEmitters(pointer: Seq[String], schemaPath: Seq[(String, String)]): Seq[Emitter] =
    emitters(pointer, schemaPath)
}
