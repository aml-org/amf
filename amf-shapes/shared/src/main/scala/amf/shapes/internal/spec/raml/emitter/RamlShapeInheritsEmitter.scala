package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.DeclaredElement
import amf.core.internal.parser.domain.FieldEntry
import amf.core.internal.render.BaseEmitters.{pos, raw}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.client.scala.model.domain.UnionShape
import amf.shapes.client.scala.model.domain.{AnyShape, ShapeHelpers, UnionShape}
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class RamlShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlShapeEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {

    val values: Seq[Shape] = f.array.values.map(_.asInstanceOf[Shape])
    val multiple           = values.size > 1

    b.entry(
      "type",
      b => {
        // If there are many values is a multiple inheritance which needs to be emitted as a seq
        if (multiple)
          b.list(l => values.foreach(emitShape(_, l)))
        else values.foreach(emitShape(_, b))
      }
    )
  }

  private def emitShape(value: Shape, b: PartBuilder): Unit = value match {
    case u: UnionShape if !u.isLink =>
      RamlInlinedUnionShapeEmitter(u, ordering, references).partEmitters().emitAll(b)
    case d: Shape with ShapeHelpers if d.annotations.contains(classOf[DeclaredElement]) =>
      emitDeclared(d, b)
    case s: AnyShape =>
      Raml10TypePartEmitter(s, ordering, None, references = references).emit(b)
    case other =>
      spec.eh.violation(TransformationValidation,
                        other.id,
                        None,
                        "Cannot emit for type shapes without WebAPI Shape support",
                        other.position(),
                        other.location())
  }

  private def emitDeclared(shape: Shape with ShapeHelpers, b: PartBuilder): Unit =
    if (shape.isLink) spec.localReference(shape).emit(b)
    else raw(b, shape.name.value())

  override def position(): Position = pos(f.value.annotations)
}
