package amf.shapes.internal.spec.oas.emitter

import amf.core.client.scala.model.domain.Shape
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.client.scala.annotations.TypePropertyLexicalInfo
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
