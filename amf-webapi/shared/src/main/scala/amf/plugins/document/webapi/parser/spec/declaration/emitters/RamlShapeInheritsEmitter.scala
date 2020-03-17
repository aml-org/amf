package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.annotations.DeclaredElement
import amf.core.emitter.BaseEmitters.{pos, raw, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.{FieldEntry, Position}
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.domain.shapes.models.{AnyShape, ShapeHelpers, UnionShape}
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class RamlShapeInheritsEmitter(f: FieldEntry, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext)
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
    case d: Shape with ShapeHelpers if d.annotations.contains(classOf[DeclaredElement]) || d.isLink =>
      emitDeclared(d, b)
    case s: AnyShape =>
      b.obj(r => traverse(Raml10TypeEmitter(s, ordering, references = references).entries(), r))
    case other =>
      spec.eh.violation(ResolutionValidation,
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
