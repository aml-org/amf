package amf.plugins.document.webapi.parser.spec.oas.emitters

import amf.core.emitter.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.domain.AmfScalar
import amf.core.parser.Position
import amf.plugins.document.webapi.contexts.SpecEmitterContext
import amf.plugins.domain.webapi.models.Tag
import org.yaml.model.YDocument.EntryBuilder

case class StringArrayTagsEmitter(key: String, tags: Seq[Tag], ordering: SpecOrdering)(
    implicit spec: SpecEmitterContext)
    extends EntryEmitter {

  override def position(): Position = tags.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)

  override def emit(b: EntryBuilder): Unit = {
    val emitters = tags.flatMap(_.name.option()).map(name => ScalarEmitter(AmfScalar(name)))
    b.entry(
      key,
      _.list(traverse(ordering.sorted(emitters), _))
    )
  }
}
