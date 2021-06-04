package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.emitter.{Emitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.{RamlShapeEmitterContext, ShapeEmitterContext}
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.annotations.AnnotationsEmitter

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
