package amf.plugins.document.webapi.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.traverse
import amf.core.emitter.{PartEmitter, SpecOrdering}
import amf.core.metamodel.Field
import amf.core.model.document.BaseUnit
import amf.core.model.domain.Shape
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import org.yaml.model.YDocument.PartBuilder

case class OasTypePartEmitter(shape: Shape,
                              ordering: SpecOrdering,
                              ignored: Seq[Field] = Nil,
                              references: Seq[BaseUnit],
                              pointer: Seq[String] = Nil,
                              schemaPath: Seq[(String, String)] = Nil)(implicit spec: OasLikeSpecEmitterContext)
    extends OasTypePartCollector(shape, ordering, ignored, references)
    with PartEmitter {

  override def emit(b: PartBuilder): Unit =
    emitter(pointer, schemaPath) match {
      case Left(p)        => p.emit(b)
      case Right(entries) => b.obj(traverse(entries, _))
    }

  override def position(): Position = getEmitters.headOption.map(_.position()).getOrElse(ZERO)

}
