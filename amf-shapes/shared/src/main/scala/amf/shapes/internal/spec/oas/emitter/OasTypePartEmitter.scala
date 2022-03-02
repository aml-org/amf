package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.Shape
import amf.core.internal.metamodel.Field
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.PartEmitter
import amf.shapes.internal.spec.common.emitter.OasLikeShapeEmitterContext
import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import org.yaml.model.YDocument.PartBuilder

case class OasTypePartEmitter(
    shape: Shape,
    ordering: SpecOrdering,
    ignored: Seq[Field] = Nil,
    references: Seq[BaseUnit],
    pointer: Seq[String] = Nil,
    schemaPath: Seq[(String, String)] = Nil
)(implicit spec: OasLikeShapeEmitterContext)
    extends OasTypePartCollector(shape, ordering, ignored, references)
    with PartEmitter {

  override def emit(b: PartBuilder): Unit =
    emitter(pointer, schemaPath) match {
      case Left(p)        => p.emit(b)
      case Right(entries) => b.obj(traverse(entries, _))
    }

  override def position(): Position = getEmitters.headOption.map(_.position()).getOrElse(ZERO)

}
