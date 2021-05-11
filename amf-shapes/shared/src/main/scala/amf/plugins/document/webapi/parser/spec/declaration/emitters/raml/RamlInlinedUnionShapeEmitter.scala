package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.parser.spec.declaration.emitters.{RamlShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.domain.shapes.models.UnionShape
import org.yaml.model.YDocument.PartBuilder

case class RamlInlinedUnionShapeEmitter(shape: UnionShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends RamlAnyShapeEmitter(shape, ordering, references) {

  def partEmitters(): MixedEmitters = {
    // If anyOf is empty and inherits is not empty, the shape is still not resolved. So, emit as a AnyShape
    val unionEmitters =
      if (shape.anyOf.isEmpty && shape.inherits.nonEmpty) Nil
      else Seq(RamlInlinedAnyOfShapeEmitter(shape, ordering, references = references))
    MixedEmitters(super.emitters(), unionEmitters)
  }

  case class MixedEmitters(entries: Seq[EntryEmitter], parts: Seq[PartEmitter]) {
    def emitAll(b: PartBuilder): Unit = {
      parts.foreach(_.emit(b))
      if (entries.nonEmpty) b.obj(b => entries.foreach(_.emit(b)))
    }
  }

  override val typeName: Option[String] = Some("union")
}
