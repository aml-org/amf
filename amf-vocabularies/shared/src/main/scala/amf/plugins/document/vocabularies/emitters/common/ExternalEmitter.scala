package amf.plugins.document.vocabularies.emitters.common

import amf.core.annotations.LexicalInformation
import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.parser.Position
import amf.core.parser.Position.ZERO
import amf.plugins.document.vocabularies.model.domain.External
import org.yaml.model.YDocument

case class ExternalEmitter(external: External, ordering: SpecOrdering) extends EntryEmitter {
  override def emit(b: YDocument.EntryBuilder): Unit =
    MapEntryEmitter(external.alias.value(), external.base.value()).emit(b)

  override def position(): Position =
    external.annotations.find(classOf[LexicalInformation]).map(_.range.start).getOrElse(ZERO)
}
