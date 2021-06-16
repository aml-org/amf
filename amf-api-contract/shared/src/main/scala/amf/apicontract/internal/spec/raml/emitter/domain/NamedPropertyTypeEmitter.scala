package amf.apicontract.internal.spec.raml.emitter.domain

import amf.apicontract.internal.spec.common.emitter.AgnosticShapeEmitterContextAdapter
import amf.apicontract.internal.spec.raml.emitter.context.RamlSpecEmitterContext
import amf.core.client.common.position.Position
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.extensions.CustomDomainProperty
import amf.core.internal.render.BaseEmitters.{pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{EntryEmitter, PartEmitter}
import amf.core.internal.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class NamedPropertyTypeEmitter(annotation: CustomDomainProperty, references: Seq[BaseUnit], ordering: SpecOrdering)(
    implicit val spec: RamlSpecEmitterContext)
    extends EntryEmitter
    with PartEmitter {

  protected implicit val shapeCtx = AgnosticShapeEmitterContextAdapter(spec)

  override def emit(b: EntryBuilder): Unit = {
    val name = annotation.name.option() match {
      case Some(n) => n
      case _ =>
        spec.eh.violation(ResolutionValidation,
                          annotation.id,
                          None,
                          s"Annotation type without name $annotation",
                          annotation.position(),
                          annotation.location())
        "default-name"
    }
    b.entry(name, b => emit(b))
  }

  override def emit(b: PartBuilder): Unit = emitLinkOr(annotation, b, references)(emitInline(b))

  private def emitInline(b: PartBuilder): Unit = {
    spec.factory.annotationTypeEmitter(annotation, ordering).emitters() match {
      case Left(emitters) =>
        b.obj { e =>
          traverse(ordering.sorted(emitters), e)
        }
      case Right(part) =>
        part.emit(b)
    }
  }

  override def position(): Position = pos(annotation.annotations)
}
