package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.pos
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.domain.shapes.models.{AnyShape, ShapeHelpers}
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
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
                                    Seq[BaseUnit]) => RamlTypePartEmitter)(implicit spec: SpecEmitterContext)
    extends EntryEmitter {
  override def emit(b: EntryBuilder): Unit = {
    val name = shape.name.option().getOrElse("schema") // this used to throw an exception, but with the resolution optimizacion, we use the father shape, so it could have not name (if it's from an endpoint for example, and you want to write a new single shape, like a json schema)
    b.entry(name, if (shape.isLink) emitLink _ else emitInline _)
  }

  private def emitLink(b: PartBuilder): Unit = {
    spec.factory.tagToReferenceEmitter(shape, references).emit(b)
  }

  private def emitInline(b: PartBuilder): Unit = shape match {
    case s: Shape with ShapeHelpers => typesEmitter(s, ordering, None, Seq(), references).emit(b)
    case _ =>
      spec.eh.violation(
        ResolutionValidation,
        shape.id,
        None,
        "Cannot emit inline shape that doesnt support type expressions",
        shape.position(),
        shape.location()
      )
  }

  override def position(): Position = pos(shape.annotations)
}
