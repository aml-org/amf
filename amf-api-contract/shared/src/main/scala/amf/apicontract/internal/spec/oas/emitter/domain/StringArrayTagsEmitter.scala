package amf.apicontract.internal.spec.oas.emitter.domain

import amf.apicontract.client.scala.model.domain.Tag
import amf.apicontract.internal.spec.common.emitter.SpecEmitterContext
import amf.core.client.common.position.Position
import amf.core.client.scala.model.domain.AmfScalar
import amf.core.internal.render.BaseEmitters.{ScalarEmitter, pos, traverse}
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.yaml.model.YDocument.EntryBuilder

case class StringArrayTagsEmitter(key: String, tags: Seq[Tag], ordering: SpecOrdering)(implicit
    spec: SpecEmitterContext
) extends EntryEmitter {

  override def position(): Position = tags.headOption.map(a => pos(a.annotations)).getOrElse(Position.ZERO)

  override def emit(b: EntryBuilder): Unit = {
    val emitters = tags.flatMap(_.name.option()).map(name => ScalarEmitter(AmfScalar(name)))
    b.entry(
      key,
      _.list(traverse(ordering.sorted(emitters), _))
    )
  }
}
