package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.render.BaseEmitters.pos
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.client.scala.model.domain.ShapeHelpers
import amf.shapes.client.scala.model.domain.{AnyShape, ShapeHelpers}
import amf.shapes.internal.spec.common.emitter.ReferenceEmitterHelper.emitLinkOr
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

/**
  *
  */
case class RamlNamedTypeEmitter(shape: AnyShape,
                                ordering: SpecOrdering,
                                references: Seq[BaseUnit] = Nil,
                                typesEmitter: (
                                    AnyShape,
                                    SpecOrdering,
                                    Option[AnnotationsEmitter],
                                    Seq[Field],
                                    Seq[BaseUnit]) => RamlTypePartEmitter)(implicit spec: ShapeEmitterContext)
    extends EntryEmitter {

  override def emit(b: EntryBuilder): Unit = {
    val name = shape.name.option().getOrElse("schema") // this used to throw an exception, but with the resolution optimization, we use the father shape, so it could have not name (if it's from an endpoint for example, and you want to write a new single shape, like a json schema)
    b.entry(name, b => emitLinkOr(shape, b, references)(emitInline(b)))
  }

  private def emitInline(b: PartBuilder): Unit = shape match {
    case s: Shape with ShapeHelpers => typesEmitter(s, ordering, None, Seq(), references).emit(b)
    case _ =>
      spec.eh.violation(
        TransformationValidation,
        shape.id,
        None,
        "Cannot emit inline shape that doesnt support type expressions",
        shape.position(),
        shape.location()
      )
  }

  override def position(): Position = pos(shape.annotations)
}
