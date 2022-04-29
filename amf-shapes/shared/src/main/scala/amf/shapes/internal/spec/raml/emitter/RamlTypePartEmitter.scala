package amf.shapes.internal.spec.raml.emitter

import amf.core.client.common.position.Position
import amf.core.client.common.position.Position.ZERO
import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.annotations.SynthesizedField
import amf.core.internal.metamodel.Field
import amf.core.internal.render.BaseEmitters.{raw, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.{Emitter, EntryEmitter, PartEmitter}
import amf.core.internal.validation.CoreValidations.TransformationValidation
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter
import org.yaml.model.YDocument.PartBuilder
import org.yaml.model.YType

abstract class RamlTypePartEmitter(
    shape: Shape,
    ordering: SpecOrdering,
    annotations: Option[AnnotationsEmitter],
    ignored: Seq[Field] = Nil,
    references: Seq[BaseUnit]
)(implicit spec: ShapeEmitterContext)
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
      spec.eh.violation(
        TransformationValidation,
        shape.id,
        None,
        s"IllegalTypeDeclarations found: $other",
        shape.position(),
        shape.location()
      )
      Right(Nil)
  }
}
