package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{Emitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter

case class Raml10TypePartEmitter(shape: Shape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter],
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {

  override def emitters: Seq[Emitter] = {
    val annotationEmitters = annotations.map(_.emitters).getOrElse(Nil)
    ordering.sorted(
      Raml10TypeEmitter(shape, ordering, ignored, references, forceEntry = annotationEmitters.nonEmpty)
        .emitters() ++ annotationEmitters)
  }

}
