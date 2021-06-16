package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.Emitter
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter

case class Raml10TypePartEmitter(shape: Shape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter],
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: RamlShapeEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {

  override def emitters: Seq[Emitter] = {
    val annotationEmitters = annotations.map(_.emitters).getOrElse(Nil)
    val typeEmitterAnnotations =
      Raml10TypeEmitter(shape, ordering, ignored, references, forceEntry = annotationEmitters.nonEmpty).emitters()
    ordering.sorted(typeEmitterAnnotations ++ annotationEmitters)
  }

}
