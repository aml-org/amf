package amf.plugins.document.webapi.parser.spec.declaration.emitters.oas

import amf.core.emitter.{Emitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.parser.spec.declaration.emitters.ShapeEmitterContext

class SimpleOasTypePartCollector(shape: Shape, ordering: SpecOrdering, ignored: Seq[Field], references: Seq[BaseUnit])(
    implicit spec: ShapeEmitterContext)
    extends OasTypePartCollector(shape, ordering, ignored, references) {

  def computeEmitters(pointer: Seq[String], schemaPath: Seq[(String, String)]): Seq[Emitter] =
    emitters(pointer, schemaPath)
}
