package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
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
