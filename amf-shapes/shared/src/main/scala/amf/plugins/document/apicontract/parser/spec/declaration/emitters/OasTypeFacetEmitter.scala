package amf.plugins.document.apicontract.parser.spec.declaration.emitters

import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.EntryEmitter
import amf.core.model.domain.Shape
import amf.plugins.domain.apicontract.annotations.TypePropertyLexicalInfo
import org.yaml.model.YType

object OasTypeFacetEmitter {

  def apply(typeName: String, shape: Shape): EntryEmitter = {
    shape.annotations.find(classOf[TypePropertyLexicalInfo]) match {
      case Some(lexicalInfo) =>
        MapEntryEmitter("type", typeName, YType.Str, lexicalInfo.range.start)
      case None =>
        MapEntryEmitter("type", typeName)
    }
  }
}
