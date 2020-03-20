package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.domain.shapes.models.UnionShape

case class RamlUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
    extends RamlAnyShapeEmitter(shape, ordering, references) {

  override def emitters(): Seq[EntryEmitter] = {
    // If anyOf is empty and inherits is not empty, the shape is still not resolved. So, emit as a AnyShape
    val unionEmitters =
      if (shape.anyOf.isEmpty && shape.inherits.nonEmpty) Nil
      else Seq(RamlAnyOfShapeEmitter(shape, ordering, references = references))
    super.emitters() ++ unionEmitters
  }

  override val typeName: Option[String] = Some("union")
}
