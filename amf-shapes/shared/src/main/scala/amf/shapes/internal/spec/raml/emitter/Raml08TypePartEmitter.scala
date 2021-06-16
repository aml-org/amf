package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.Emitter
import amf.shapes.internal.spec.common.emitter.RamlShapeEmitterContext
import amf.shapes.internal.spec.common.emitter.annotations.AnnotationsEmitter

object Raml08TypePartEmitter {
  def apply(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: RamlShapeEmitterContext): Raml08TypePartEmitter =
    new Raml08TypePartEmitter(shape, ordering, None, Seq(), Seq())
}

case class Raml08TypePartEmitter(shape: Shape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter] = None,
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: RamlShapeEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {
  override def emitters: Seq[Emitter] = Raml08TypeEmitter(shape, ordering).emitters()
}
