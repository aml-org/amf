package amf.shapes.internal.spec.raml.emitter

import amf.core.client.scala.model.document.BaseUnit
import amf.core.client.scala.model.domain.RecursiveShape
import amf.core.internal.render.BaseEmitters.MapEntryEmitter
import amf.core.internal.render.SpecOrdering
import amf.core.internal.render.emitters.EntryEmitter
import amf.shapes.internal.spec.common.emitter.ShapeEmitterContext

import scala.collection.mutable.ListBuffer
import amf.core.internal.utils._

case class RamlRecursiveShapeEmitter(shape: RecursiveShape, ordering: SpecOrdering, references: Seq[BaseUnit])(implicit
    spec: ShapeEmitterContext
) {
  def emitters(): Seq[EntryEmitter] = {
    val result: ListBuffer[EntryEmitter] = ListBuffer()
    result += MapEntryEmitter("type", "object")
    result += MapEntryEmitter("recursive".asRamlAnnotation, shape.fixpoint.value())
    result
  }
}
