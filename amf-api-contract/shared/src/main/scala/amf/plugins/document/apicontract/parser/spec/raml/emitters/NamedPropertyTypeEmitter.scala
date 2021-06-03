package amf.plugins.document.apicontract.parser.spec.raml.emitters

import amf.core.emitter.BaseEmitters.{pos, traverse}
import amf.core.emitter.{EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.extensions.CustomDomainProperty
import amf.core.parser.Position
import amf.plugins.document.apicontract.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.ReferenceEmitterHelper.emitLinkOr
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.AgnosticShapeEmitterContextAdapter
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.{EntryBuilder, PartBuilder}

case class NamedPropertyTypeEmitter(annotation: CustomDomainProperty,
                                    references: Seq[BaseUnit],
                                    ordering: SpecOrdering)(implicit val spec: RamlSpecEmitterContext)
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
