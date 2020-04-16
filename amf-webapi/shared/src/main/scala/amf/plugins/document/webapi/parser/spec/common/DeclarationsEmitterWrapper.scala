package amf.plugins.document.webapi.parser.spec.common

import amf.core.emitter.BaseEmitters.traverse
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import org.yaml.model.YDocument

case class DeclarationsEmitterWrapper(emitters: Seq[EntryEmitter], ordering: SpecOrdering) extends EntryEmitter {

  override def emit(b: YDocument.EntryBuilder): Unit =
    if (emitters.nonEmpty)
      b.entry("components", _.obj { b =>
        traverse(ordering.sorted(emitters), b)
      })

  // Position of any of the components.
  override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)
}
