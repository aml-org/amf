package amf.plugins.document.apicontract.parser.spec.declaration.emitters.raml

import amf.core.emitter.BaseEmitters.MapEntryEmitter
import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.core.model.domain.RecursiveShape
import amf.core.utils.AmfStrings
import amf.plugins.document.apicontract.parser.spec.declaration.emitters.ShapeEmitterContext

import scala.collection.mutable.ListBuffer

case class RamlRecursiveShapeEmitter(shape: RecursiveShape, ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: ShapeEmitterContext) {
  def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer()
    result += MapEntryEmitter("type", "object")
    result += MapEntryEmitter("recursive".asRamlAnnotation, shape.fixpoint.value())
    result
  }
}
