package amf.plugins.document.webapi.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.RecursiveShape
import amf.plugins.document.webapi.contexts.emitter.raml.RamlSpecEmitterContext
import amf.core.utils.AmfStrings

import scala.collection.mutable.ListBuffer

case class RamlRecursiveShapeEmitter(shape: RecursiveShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: RamlSpecEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer()
    result += MapEntryEmitter("type", "object")
    result += MapEntryEmitter("recursive".asRamlAnnotation, shape.fixpoint.value())
    result
  }
}
