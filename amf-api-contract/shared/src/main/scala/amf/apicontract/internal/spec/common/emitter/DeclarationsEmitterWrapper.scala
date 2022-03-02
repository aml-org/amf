package amf.apicontract.internal.spec.common.emitter

import org.mulesoft.common.client.lexical.Position
import org.mulesoft.common.client.lexical.Position.ZERO
import amf.core.internal.render.BaseEmitters.traverse
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import org.yaml.model.YDocument

case class DeclarationsEmitterWrapper(emitters: Seq[EntryEmitter], ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit =
    if (emitters.nonEmpty)
      b.entry(
        "components",
        _.obj { b =>
          traverse(ordering.sorted(emitters), b)
        }
      )

  // Position of any of the components.
  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)
}
