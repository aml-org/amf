package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.annotations.SynthesizedField
import amf.core.emitter.BaseEmitters.{raw, traverse}
import amf.core.emitter.{Emitter, EntryEmitter, PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.AnnotationsEmitter
import amf.plugins.features.validation.CoreValidations.ResolutionValidation
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

abstract class RamlTypePartEmitter(shape: Shape,
                                   ordering: SpecOrdering,
                                   annotations: Option[AnnotationsEmitter],
                                   ignored: Seq[Field] = Nil,
                                   references: Seq[BaseUnit])(implicit spec: ShapeEmitterContext)
    extends PartEmitter {

  override def emit(b: PartBuilder): Unit = {
    if (Option(shape).isDefined && shape.annotations.contains(classOf[SynthesizedField])) {
      raw(b, "", YType.Null)
    } else {
      emitter match {
        case Left(p)        => p.emit(b)
        case Right(entries) => b.obj(traverse(ordering.sorted(entries), _))
      }
    }
  }

  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)

  protected def emitters: Seq[Emitter]

  val emitter: Either[PartEmitter, Seq[EntryEmitter]] = emitters match {
    case Seq(p: PartEmitter)                           => Left(p)
    case es if es.forall(_.isInstanceOf[EntryEmitter]) => Right(es.collect { case e: EntryEmitter => e })
    case other =>
      spec.eh.violation(ResolutionValidation,
                        shape.id,
                        None,
                        s"IllegalTypeDeclarations found: $other",
                        shape.position(),
                        shape.location())
      Right(Nil)
  }
}
