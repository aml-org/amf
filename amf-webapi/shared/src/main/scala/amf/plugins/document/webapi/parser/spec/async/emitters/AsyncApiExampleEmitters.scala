package amf.plugins.document.webapi.parser.spec.async.emitters

import amf.core.emitter.{EntryEmitter, SpecOrdering}
import amf.core.model.document.BaseUnit
import amf.plugins.document.webapi.contexts.emitter.OasLikeSpecEmitterContext
import amf.plugins.document.webapi.parser.spec.domain.ExampleArrayEmitter
import amf.plugins.document.webapi.parser.spec.oas.emitters.OasLikeExampleEmitters
import amf.plugins.domain.shapes.models.Example

import scala.collection.mutable.ListBuffer

object AsyncApiExampleEmitters {

  def apply(examples: Seq[Example], ordering: SpecOrdering, references: Seq[BaseUnit])(
      implicit spec: OasLikeSpecEmitterContext): AsyncApiExampleEmitters = {
    new AsyncApiExampleEmitters(examples, ordering, references)
  }
}

class AsyncApiExampleEmitters(examples: Seq[Example], ordering: SpecOrdering, references: Seq[BaseUnit])(
    implicit spec: OasLikeSpecEmitterContext)
    extends OasLikeExampleEmitters {
  override def emitters(): ListBuffer[EntryEmitter] = {
    ListBuffer(ExampleArrayEmitter("examples", examples, ordering, references))
  }
}
