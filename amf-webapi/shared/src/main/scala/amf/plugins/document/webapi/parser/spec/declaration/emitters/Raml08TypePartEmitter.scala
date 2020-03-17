package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.{Emitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.declaration.AnnotationsEmitter

object Raml08TypePartEmitter {
  def apply(shape: Shape, ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: RamlSpecEmitterContext): Raml08TypePartEmitter =
    new Raml08TypePartEmitter(shape, ordering, None, Seq(), Seq())
}

case class Raml08TypePartEmitter(shape: Shape,
                                 ordering: SpecOrdering,
                                 annotations: Option[AnnotationsEmitter] = None,
                                 ignored: Seq[Field] = Nil,
                                 references: Seq[BaseUnit])(implicit spec: RamlSpecEmitterContext)
    extends RamlTypePartEmitter(shape, ordering, annotations, ignored, references) {
  override def emitters: Seq[Emitter] = Raml08TypeEmitter(shape, ordering).emitters()
}
