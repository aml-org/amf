package amf.plugins.document.webapi.parser.spec.oas

import amf.core.emitter.BaseEmitters.traverse
import amf.core.emitter.{SpecOrdering, EntryEmitter}
import amf.core.model.document.BaseUnit
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.webapi.contexts.emitter.oas.OasSpecEmitterContext
import org.yaml.model.{YType, YScalar, YDocument, YNode}

case class Oas3DocumentEmitter(document: BaseUnit)(implicit override val spec: OasSpecEmitterContext)
    extends OasDocumentEmitter(document) {

  override protected def versionEntry(b: YDocument.EntryBuilder): Unit =
    b.openapi = YNode(YScalar("3.0.0"), YType.Str) // this should not be necessary but for use the same logic

  override protected def wrapDeclarations(emitters: Seq[EntryEmitter], ordering: SpecOrdering): Seq[EntryEmitter] =
    Seq(DeclarationsWrapper(emitters, ordering))

  case class DeclarationsWrapper(emitters: Seq[EntryEmitter], ordering: SpecOrdering) extends EntryEmitter {

    override def emit(b: YDocument.EntryBuilder): Unit =
      if (emitters.nonEmpty)
        b.entry("components", _.obj { b =>
          traverse(ordering.sorted(emitters), b)
        })

    // Position of any of the components.
    override def position(): Position = emitters.headOption.map(_.position()).getOrElse(ZERO)
  }
}
